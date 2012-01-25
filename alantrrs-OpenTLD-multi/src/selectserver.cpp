#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <opencv2/opencv.hpp>
#include <tld_utils.h>
#include <iostream>
#include <sstream>
#include <TLD.h>

#define YES	"Yes"
#define NO	"No"
#define SERVER_BANNER	"Hi! Ask me a question and I'll say Yes or No"
#define MAX_CLIENTS	5
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

void error(const char *msg)
{
	perror(msg);
	exit(1);
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

void send_box(int sockfd, bool status, Rect box)
{
	int n;
	char* buffer = (char *) malloc (sizeof(char) * 1024);
	sprintf(buffer, "%d:%d:%d:%d:%d", (status ? 1 : 0), box.x, box.y, box.width, box.height);
	buffer[strlen(buffer)] = 0;
	n = send(sockfd, buffer, strlen(buffer) + 1, 0);
	if (n < 0) 
		error("ERROR writing to socket");
	
	wait_reply(sockfd);	
	
	free(buffer);
}

Mat receive_mat(int i)
{
	int n, j = 0, to_recv = 10000;
	int rows, cols, elem_size, elem_type, step, dimension;
	Mat mat;
	char* buffer = (char *) malloc (sizeof(char) * BUFFSIZ);
	char *pch;
	
	n = recv(i, buffer, to_recv, 0);
	if(n <= 0) 
		return Mat(0, 0, 0, NULL, 0);
#ifdef DEBUG	
	printf("Mat information: %s\n", buffer);
#endif
	pch = strtok (buffer,":");
	rows = atoi(pch);
	pch = strtok (NULL, ":");
	cols = atoi(pch);
	pch = strtok (NULL, ":");
	elem_size = atoi(pch);
	pch = strtok (NULL, ":");
	elem_type = atoi(pch);
	pch = strtok (NULL, ":");
	step = atoi(pch);
	pch = strtok (NULL, ":");
	dimension = atoi(pch);
	int no_chunks = ceil(dimension / 1024);
	to_recv = 1024;
	uchar * matr = (uchar *) malloc (dimension);
	while(1)
	{
		if(j == no_chunks)
			break;
		n = recv(i, buffer, 10, 0);
		to_recv = atoi(buffer);
		send_reply(i);
		
		n = recv(i, buffer, to_recv, 0);
		memcpy(matr + (j * 1024), buffer, to_recv);
		send_reply(i);
		
		j++;
	}
	mat = Mat(rows, cols, elem_type, matr, step);
	free(buffer);
	return mat;
}

Rect receive_box(int i)
{
	int n, to_recv = 1024, box_x, box_y, box_width, box_height;
	char* buffer = (char *) malloc (sizeof(char) * BUFFSIZ);
	char* pch;
	
	n = recv(i, buffer, to_recv, 0);
	pch = strtok (buffer,":");
	box_x = atoi(pch);
	pch = strtok (NULL, ":");
	box_y = atoi(pch);
	pch = strtok (NULL, ":");
	box_width = atoi(pch);
	pch = strtok (NULL, ":");
	box_height = atoi(pch);
	send_reply(i);
	box = Rect(box_x, box_y, box_width, box_height);
	free(buffer);
	return box;
}

void receive_tld_params(int i, char *buffer, struct TLDParams &p1, struct ferNNParams &p2)
{
	char* pch;
	int n, to_recv = 4096;
	
	/**Get FerNNClassifier struct information**/
#ifdef DEBUG
	printf("\nFerNNClassifier struct: %s\n", buffer);
#endif
	pch = strtok (buffer,":");
	p2.valid = atof(pch);
	pch = strtok (NULL, ":");
	p2.ncc_thesame = atof(pch);
	pch = strtok (NULL, ":");
	p2.nstructs = atoi(pch);
	pch = strtok (NULL, ":");
	p2.structSize = atoi(pch);
	pch = strtok (NULL, ":");
	p2.thr_fern = atof(pch);
	pch = strtok (NULL, ":");
	p2.thr_nn = atof(pch);
	pch = strtok (NULL, ":");
	p2.thr_nn_valid = atof(pch);
	send_reply(i);
	
	/**Get TLD struct information**/
	n = recv(i, buffer, to_recv, 0);
#ifdef DEBUG	
	printf("TLD struct: %s\n", buffer);
#endif
	pch = strtok (buffer,":");
	p1.min_win = atoi(pch);
	pch = strtok (NULL, ":");
	p1.patch_size = atoi(pch);
	pch = strtok (NULL, ":");
	p1.num_closest_init = atoi(pch);
	pch = strtok (NULL, ":");
	p1.num_warps_init = atoi(pch);
	pch = strtok (NULL, ":");
	p1.noise_init = atoi(pch);
	pch = strtok (NULL, ":");
	p1.angle_init = atof(pch);
	pch = strtok (NULL, ":");
	p1.shift_init = atof(pch);
	pch = strtok (NULL, ":");
	p1.scale_update = atof(pch);
	pch = strtok (NULL, ":");
	p1.bad_overlap = atof(pch);
	pch = strtok (NULL, ":");
	p1.bad_patches = atoi(pch);
	send_reply(i);
}

int main(int argc, char *argv[])
{
	int sockfd, newsockfd, portno;
	socklen_t clilen;
	int to_recv = 10000;
	char *buffer = (char *) malloc (sizeof(char) * BUFFSIZ);
	struct sockaddr_in serv_addr, cli_addr;
	int n, i, j;

	fd_set read_fds;	//fd_set folosit in select()
	fd_set tmp_fds;	//fd_set folosit temporar
	int fdmax;		//nr maxim de file descriptori

	//golim read_fds
	FD_ZERO(&read_fds);
	FD_ZERO(&tmp_fds);

	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) 
		error("ERROR opening socket");

	portno = PORT;//atoi(argv[1]);

	memset((char *) &serv_addr, 0, sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;	// foloseste adresa IP a masinii
	serv_addr.sin_port = htons(portno);
     
	if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(struct sockaddr)) < 0) 
		error("ERROR on binding");

	listen(sockfd, MAX_CLIENTS);

	//adaugam noul file descriptor in multimea read_fds
	FD_SET(sockfd, &read_fds);
	fdmax = sockfd;

	// main loop
	for(;;) 
	{
		tmp_fds = read_fds; 
		if (select(fdmax + 1, &tmp_fds, NULL, NULL, NULL) == -1)
			error("ERROR in select");
			
		for(i = 0; i <= fdmax; i++) 
		{
			if (FD_ISSET(i, &tmp_fds)) 
			{
				if (i == sockfd) 
				{
					// o noua conexiune
					clilen = sizeof(cli_addr);
					if ((newsockfd = accept(sockfd, (struct sockaddr *)&cli_addr, &clilen)) == -1) 
					{
						error("ERROR in accept");
					} 
					else 
					{
						FD_SET(newsockfd, &read_fds);
						if (newsockfd > fdmax) 
						{ 
							fdmax = newsockfd;
						}
#ifdef DEBUG
						printf("selectserver: new connection from %s\n ", inet_ntoa(cli_addr.sin_addr));
#endif
						n = send(newsockfd, SERVER_BANNER, strlen(SERVER_BANNER), 0);
     					if (n < 0)
	     					error("ERROR writing to socket");
					}
				}
				else
				{
					// am primit date
					if ((n = recv(i, buffer, to_recv, 0)) <= 0) 
					{
						if (n == 0) 
						{
							//conexiunea s-a inchis
#ifdef DEBUG
							printf("selectserver: socket %d hung up\n", i);
#endif
						} 
						else 
						{
							error("ERROR in recv");
						}
						
						close(i); 
						FD_CLR(i, &read_fds); // il scoatem din set
					} 
					else 
					{

						Rect box;
						//TLD framework
						TLD tld, tld2;
						Mat last_gray;
						Mat frame;
						struct TLDParams p1;
						struct ferNNParams p2;
						
						receive_tld_params(i, buffer, p1, p2);
						
						//Read parameters file
						tld.read(p1, p2);
						//tld2.read(p1, p2);
						
						/**Get Bounding Box information**/
						box = receive_box(i);
						
						/**Get last_gray Mat information**/
						last_gray = receive_mat(i);//Mat(rows, cols, elem_type, matr, step);
#ifdef DEBUG_MAT
						printf("%s\n", last_gray.ptr());
#endif
						
						//Output file
						FILE  *bb_file = fopen("bounding_boxes.txt","w");
						//FILE  *bb_file2 = fopen("bounding_boxes2.txt","w");
						//TLD initialization
						tld.init(last_gray, box, bb_file);
						//tld2.init(last_gray2,box2,bb_file2);
						
						Mat current_gray, current_gray2;
						BoundingBox pbox, pbox2;
						vector<Point2f> pts1, pts12;
						vector<Point2f> pts2, pts22;
						bool status=true, status2=true;
						int frames = 1;
						int detections = 1, detections2 = 1;
						
						
						while(1)
						{
							frame = receive_mat(i);
							if(frame.empty()) break;
							//get frame
							cvtColor(frame, current_gray, CV_RGB2GRAY);
							//cvtColor(frame, current_gray2, CV_RGB2GRAY);
							//Process Frame
							tld.processFrame(last_gray,current_gray,pts1,pts2,pbox,status,tl,bb_file);
#ifdef DEBUG							
							printf("%d %d %d %d\n", pbox.x, pbox.y, pbox.width, pbox.height);
#endif
							//tld2.processFrame(last_gray2,current_gray2,pts12,pts22,pbox2,status2,tl,bb_file2);
						
							//de trimis client-ului pbox x, y, width, height
							send_box(i, status, pbox);
						
							//swap points and images
							swap(last_gray,current_gray);
							pts1.clear();
							pts2.clear();
							//swap(last_gray2,current_gray2);
							//pts12.clear();
							//pts22.clear();
							imshow("TLD", frame);
						}
					}
				} 
			}
		}
	}

	free(buffer);
	close(sockfd);

	return 0; 
}


