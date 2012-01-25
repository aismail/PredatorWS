#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h> 
#include <opencv2/opencv.hpp>
#include <tld_utils.h>
#include <iostream>
#include <sstream>
#include <TLD.h>

#define YES	"Yes"
#define NO	"No"
#define HOSTNAME "localhost"
#define PORT 8889
#define BUFFSIZ 4096

using namespace cv;
using namespace std;

//Global variables
Rect box;
Rect box2;
bool hasFirstBox = false, hasSecondBox = false;
bool drawing_box = false;
bool gotBB = false;
bool tl = false;
bool rep = false;
bool fromfile=false;
string video;

void readBB(char* file)
{
	ifstream bb_file (file);
	string line;
	getline(bb_file,line);
	istringstream linestream(line);
	string x1,y1,x2,y2;
	getline (linestream,x1, ',');
	getline (linestream,y1, ',');
	getline (linestream,x2, ',');
	getline (linestream,y2, ',');
	int x = atoi(x1.c_str());// = (int)file["bb_x"];
	int y = atoi(y1.c_str());// = (int)file["bb_y"];
	int w = atoi(x2.c_str())-x;// = (int)file["bb_w"];
	int h = atoi(y2.c_str())-y;// = (int)file["bb_h"];
	box = Rect(x,y,w,h);
}
//bounding box mouse callback
void mouseHandler(int event, int x, int y, int flags, void *param)
{
	Rect *pbox;
	switch( event ){
	case CV_EVENT_MOUSEMOVE:
		if (drawing_box)
		{
			if(hasSecondBox)
				pbox = &box2;
			else
				pbox = &box;
			pbox->width = x-pbox->x;
			pbox->height = y-pbox->y;
		}
		break;
	case CV_EVENT_LBUTTONDOWN:
		drawing_box = true;
		if(hasSecondBox)
		box = box2;
		if(hasFirstBox)
		{
			box2 = Rect( x, y, 0, 0 );
			hasSecondBox = true;
		}
		else
		{
			box = Rect( x, y, 0, 0 );
			hasFirstBox = true;
		}
		break;
	case CV_EVENT_LBUTTONUP:
		drawing_box = false;
		if(hasSecondBox)
			pbox = &box2;
		else
			pbox = &box;
		if( pbox->width < 0 )
		{
			pbox->x += pbox->width;
			pbox->width *= -1;
		}
		if( pbox->height < 0 )
		{
			pbox->y += pbox->height;
			pbox->height *= -1;
		}
		if(hasSecondBox)
			gotBB = true;
		break;
	}
}

void print_help(char** argv)
{
	printf("use:\n     %s -p /path/parameters.yml\n",argv[0]);
	printf("-s    source video\n-b        bounding box file\n-tl  track and learn\n-r     repeat\n");
}

void read_options(int argc, char** argv,VideoCapture& capture,FileStorage &fs)
{
	for (int i=0;i<argc;i++)
		{
		if (strcmp(argv[i],"-b")==0)
		{
			if (argc>i)
			{
				readBB(argv[i+1]);
				gotBB = true;
			}
			else
				print_help(argv);
		}
		if (strcmp(argv[i],"-s")==0)
		{
			if (argc>i)
			{
				video = string(argv[i+1]);
				capture.open(video);
				fromfile = true;
			}
			else
				print_help(argv);
		}
		if (strcmp(argv[i],"-p")==0)
		{
			if (argc>i)
			{
				fs.open(argv[i+1], FileStorage::READ);
			}
			else
				print_help(argv);
		}
		if (strcmp(argv[i],"-tl")==0)
		{
			tl = true;
		}
		if (strcmp(argv[i],"-r")==0)
		{
			rep = true;
		}
	}
}

void error(const char *msg)
{
	perror(msg);
	exit(0);
}

void wait_reply(int sockfd)
{
	char *buffer = (char *) malloc (sizeof(char) * 256);
	int n;
	memset(buffer, 0, 255);
	n = recv(sockfd, buffer, 255, 0);
	if (n < 0) 
		error("ERROR reading from socket");
	free(buffer);
}

void send_fs(int sockfd, const FileNode& file)
{	
	float valid = (float)file["valid"];
	float ncc_thesame = (float)file["ncc_thesame"];
	int nstructs = (int)file["num_trees"];
	int structSize = (int)file["num_features"];
	float thr_fern = (float)file["thr_fern"];
	float thr_nn = (float)file["thr_nn"];
	float thr_nn_valid = (float)file["thr_nn_valid"];
	int n;
	char* buffer = (char *) malloc (sizeof(char) * 2048);
	
	sprintf(buffer, "%f:%f:%d:%d:%f:%f:%f", valid, ncc_thesame, nstructs, 
		structSize, thr_fern, thr_nn, thr_nn_valid);
	//buffer[strlen(buffer)] = 0;
	n = send(sockfd, buffer, strlen(buffer) + 1, 0);
	if (n < 0) 
		error("ERROR writing to socket");
	wait_reply(sockfd);
	
	///Bounding Box Parameters
	int min_win = (int)file["min_win"];
	///Genarator Parameters
	//initial parameters for positive examples
	int patch_size = (int)file["patch_size"];
	int num_closest_init = (int)file["num_closest_init"];
	int num_warps_init = (int)file["num_warps_init"];
	int noise_init = (int)file["noise_init"];
	float angle_init = (float)file["angle_init"];
	float shift_init = (float)file["shift_init"];
	float scale_init = (float)file["scale_init"];
	//update parameters for positive examples
	int num_closest_update = (int)file["num_closest_update"];
	int num_warps_update = (int)file["num_warps_update"];
	int noise_update = (int)file["noise_update"];
	float angle_update = (float)file["angle_update"];
	float shift_update = (float)file["shift_update"];
	float scale_update = (float)file["scale_update"];
	//parameters for negative examples
	float bad_overlap = (float)file["overlap"];
	int bad_patches = (int)file["num_patches"];
	
	memset(buffer, 0, 2048);
	sprintf(buffer, "%d:%d:%d:%d:%d:%f:%f:%f:%d:%d:%d:%f:%f:%f:%f:%d", min_win, patch_size, num_closest_init, num_warps_init,
		noise_init, angle_init, shift_init, scale_init, num_closest_update, num_warps_update, noise_update, angle_update, 
		shift_update, scale_update, bad_overlap, bad_patches);
	//buffer[strlen(buffer)] = 0;
	n = send(sockfd, buffer, strlen(buffer) + 1, 0);
	if (n < 0) 
		error("ERROR writing to socket");
	wait_reply(sockfd);
	
	free(buffer);
}

void send_box(int sockfd, Rect box)
{
	int n;
	char* buffer = (char *) malloc (sizeof(char) * 1024);
	sprintf(buffer, "%d:%d:%d:%d", box.x, box.y, box.width, box.height);
	buffer[strlen(buffer)] = 0;
	n = send(sockfd, buffer, strlen(buffer) + 1, 0);
	if (n < 0) 
		error("ERROR writing to socket");
	wait_reply(sockfd);
	free(buffer);
}

void send_mat(int sockfd, const Mat& m, const char* label)
{
	int n;
	char *buffer;
	size_t elem_size = m.elemSize();
	size_t elem_type = m.type();
	size_t dimension = (m.rows*m.cols*elem_size);
	buffer = (char *) malloc (sizeof(char) * dimension * 512);
	sprintf(buffer, "%d:%d:%d:%d:%d:%d", m.rows, m.cols, elem_size, elem_type, m.step1(), dimension);
	buffer[strlen(buffer)] = 0;
	n = send(sockfd, buffer, strlen(buffer) + 1, 0);
	if (n < 0) 
		error("ERROR writing to socket");
	
	const uchar* mat = m.ptr();
	int i = 0, size_to_send;
	int to_send = ceil(dimension / 1024);
	
	int step = 0;
	while(i < dimension)
	{
		size_to_send = dimension - i;
		if(size_to_send > 1024)
			size_to_send = 1024;
		
		memset(buffer, 0, dimension);
		sprintf(buffer, "%d", size_to_send);
		n = send(sockfd, buffer, strlen(buffer) + 1, 0);
		if (n < 0) 
			error("ERROR writing to socket");
			
		wait_reply(sockfd);
			
		n = send(sockfd, mat + i, size_to_send, 0);
		if (n < 0) 
			error("ERROR writing to socket");
		
		wait_reply(sockfd);
		
		i+=size_to_send;
		step++;
	}	
	
	free(buffer);
}

void send_reply(int sockfd)
{
	int n;
	if (random() % 2 == 0)
		n = send(sockfd, YES, strlen(YES), 0); 
	else
		n = send(sockfd, NO, strlen(NO), 0) ;
	if (n < 0)
		error("ERROR in send");
}

void receive_bbox(int sockfd, bool &status, BoundingBox &box)
{
	int n, to_recv = 1024, ok;
	char* buffer = (char *) malloc (sizeof(char) * BUFFSIZ);
	char* pch;
	
	n = recv(sockfd, buffer, to_recv, 0);
	pch = strtok (buffer,":");
	ok = atoi(pch);
	status = ok == 1 ? true : false;
	pch = strtok (NULL, ":");
	box.x = atoi(pch);
	pch = strtok (NULL, ":");
	box.y = atoi(pch);
	pch = strtok (NULL, ":");
	box.width = atoi(pch);
	pch = strtok (NULL, ":");
	box.height = atoi(pch);
	
	send_reply(sockfd);
	
	free(buffer);
}

int opentld_run(int argc, char * argv[])
{
	/**Start Init Sockets**/
	int sockfd, portno, n;
	struct sockaddr_in serv_addr;
	struct hostent *server;

	char buffer[256];
	if (argc < 3) 
	{
		fprintf(stderr,"Usage %s hostname port\n", argv[0]);
		exit(0);
	}

	portno = PORT;//atoi(argv[2]);

	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) 
		error("ERROR opening socket");

	server = gethostbyname(HOSTNAME);
	if (server == NULL) 
	{
		fprintf(stderr,"ERROR, no such host\n");
		exit(0);
	}
	
	memset((char *) &serv_addr, 0, sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	memcpy((char *)&serv_addr.sin_addr.s_addr,
		(char *)server->h_addr, 
		server->h_length);
	serv_addr.sin_port = htons(portno);

	if (connect(sockfd,(struct sockaddr*) &serv_addr,sizeof(serv_addr)) < 0) 
		error("ERROR connecting");

	wait_reply(sockfd);
	
	/**End Init Sockets**/

	VideoCapture capture;
	capture.open(0);
	FileStorage fs;
	//Read options
	read_options(argc,argv,capture,fs);
	//Init camera
	if (!capture.isOpened())
	{
		cout << "capture device failed to open!" << endl;
		return 1;
	}
	//Register mouse callback to draw the bounding box
	cvNamedWindow("TLD",CV_WINDOW_AUTOSIZE);
	cvSetMouseCallback( "TLD", mouseHandler, NULL );
	//TLD framework
	TLD tld, tld2;
	//Read parameters file
	tld.read(fs.getFirstTopLevelNode());
	tld2.read(fs.getFirstTopLevelNode());
	Mat frame;
	Mat last_gray, last_gray2;
	Mat first;
	if (fromfile)
	{
		capture >> frame;
		cvtColor(frame, last_gray, CV_RGB2GRAY);
		cvtColor(frame, last_gray2, CV_RGB2GRAY);
		frame.copyTo(first);
	}
	else
	{
		capture.set(CV_CAP_PROP_FRAME_WIDTH,340);
		capture.set(CV_CAP_PROP_FRAME_HEIGHT,240);
	}

  ///Initialization
GETBOUNDINGBOX:
	while(!gotBB)
	{
		if (!fromfile)
		{
		  capture >> frame;
		}
		else
			first.copyTo(frame);
		cvtColor(frame, last_gray, CV_RGB2GRAY);
		drawBox(frame,box);
		if(hasSecondBox)
		{
			cvtColor(frame, last_gray2, CV_RGB2GRAY);
			drawBox(frame,box2);
		}
		imshow("TLD", frame);
		if (cvWaitKey(33) == 'q')
			return 0;
	}
	if(hasSecondBox)
	{
		if (min(box2.width,box2.height)<(int)fs.getFirstTopLevelNode()["min_win"])
		{
			cout << "Bounding box too small, try again." << endl;
			hasSecondBox = false;
			gotBB = false;
			goto GETBOUNDINGBOX;
		}
	}
	else
	if (min(box.width,box.height)<(int)fs.getFirstTopLevelNode()["min_win"])
	{
		cout << "Bounding box too small, try again." << endl;
		hasFirstBox = false;
		gotBB = false;
		goto GETBOUNDINGBOX;
	}
	//Remove callback
	cvSetMouseCallback( "TLD", NULL, NULL );
#ifdef DEBUG
	printf("Initial Bounding Box = x:%d y:%d w:%d h:%d\n",box.x,box.y,box.width,box.height);
	printf("Initial Bounding Box = x:%d y:%d w:%d h:%d\n",box2.x,box2.y,box2.width,box2.height);
#endif
	/**Sart Send fs**/
	send_fs(sockfd, fs.getFirstTopLevelNode());
	
	/**Start Send Param**/
	send_box(sockfd, box);
	
	/**Last Gray**/
	send_mat(sockfd, last_gray, "last_gray");
#ifdef DEBUG_MAT
	printf("%s\n", last_gray.ptr());
#endif
	
	/**End Send Param**/
	
	//Output file
	FILE  *bb_file = fopen("bounding_boxes.txt","w");
	FILE  *bb_file2 = fopen("bounding_boxes2.txt","w");
	//TLD initialization
	tld.init(last_gray,box,bb_file);
	tld2.init(last_gray2,box2,bb_file2);

	///Run-time
	Mat current_gray, current_gray2;
	BoundingBox pbox, pbox2;
	vector<Point2f> pts1, pts12;
	vector<Point2f> pts2, pts22;
	bool status=true, status2=true;
	int frames = 1;
	int detections = 1, detections2 = 1;
REPEAT:
	while(capture.read(frame))
	{
		send_mat(sockfd, frame, "frame");
		receive_bbox(sockfd, status, pbox);
		//pe server
		//get frame
		//cvtColor(frame, current_gray, CV_RGB2GRAY);
		//cvtColor(frame, current_gray2, CV_RGB2GRAY);
		//Process Frame
		//tld.processFrame(last_gray,current_gray,pts1,pts2,pbox,status,tl,bb_file);
		//tld2.processFrame(last_gray2,current_gray2,pts12,pts22,pbox2,status2,tl,bb_file2);
		//stop pe server
		
		//Draw Points
		if (status)
		{
			//drawPoints(frame,pts1);
			//drawPoints(frame,pts2,Scalar(0,255,0));
			drawBox(frame,pbox);
			detections++;
		}
		/*if (status2)
		{
			//drawPoints(frame,pts12);
			//drawPoints(frame,pts22,Scalar(0,255,0));
			drawBox(frame,pbox2);
			detections2++;
		}*/
		//Display
		//    imshow("TLD", frame);
		//swap points and images
		//start pe server
		//swap(last_gray,current_gray);
		//pts1.clear();
		//pts2.clear();
		//swap(last_gray2,current_gray2);
		//pts12.clear();
		//pts22.clear();
		//stop pe server
		frames++;
#ifdef DEBUG
		printf("Detection rate: %d/%d\n",detections,frames);
		printf("Detection2 rate: %d/%d\n",detections2,frames);
#endif
		if (cvWaitKey(33) == 'q')
			break;
#ifdef DEBUG	
		printf("---------------------------------------------------------\n");
#endif
		imshow("TLD", frame);
	}
	if (rep)
	{
		rep = false;
		tl = false;
		fclose(bb_file);
		fclose(bb_file2);
		bb_file = fopen("final_detector.txt","w");
		//capture.set(CV_CAP_PROP_POS_AVI_RATIO,0);
		capture.release();
		capture.open(video);
		goto REPEAT;
	}
	fclose(bb_file);
	fclose(bb_file2);
	return 0;
}

int main(int argc, char *argv[])
{
	opentld_run(argc, argv);
	return 0;
}

