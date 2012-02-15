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
#define MAX_CLIENTS	10
#define PORT 8888
#define BUFFSIZ 4096
#define CHUNK_SIZE 1024

using namespace cv;
using namespace std;

//Global variables
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
	char* buffer = (char *) malloc (sizeof(char) * CHUNK_SIZE);
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
	int n, j = 0, to_recv = BUFFSIZ;
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
	int no_chunks = ceil(dimension / CHUNK_SIZE);
	to_recv = CHUNK_SIZE;
	uchar * matr = (uchar *) malloc (dimension);
	while(1)
	{
		if(j == no_chunks)
			break;
		n = recv(i, buffer, 10, 0);
		to_recv = atoi(buffer);
		send_reply(i);
		
		n = recv(i, buffer, to_recv, 0);
		memcpy(matr + (j * CHUNK_SIZE), buffer, to_recv);
		send_reply(i);
		
		j++;
	}
	mat = Mat(rows, cols, elem_type, matr, step);
	free(buffer);
	return mat;
}

Rect receive_box(int i)
{
	int n, to_recv = CHUNK_SIZE, box_x, box_y, box_width, box_height;
	char* buffer = (char *) malloc (sizeof(char) * BUFFSIZ);
	char* pch;
	Rect box;
	
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
	int n, to_recv = BUFFSIZ;
	
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

Rect box[MAX_CLIENTS], box2[MAX_CLIENTS];
//TLD framework
TLD tld[MAX_CLIENTS], tld2[MAX_CLIENTS];
Mat last_gray[MAX_CLIENTS], last_gray2[MAX_CLIENTS];
Mat frame[MAX_CLIENTS];
struct TLDParams p1[MAX_CLIENTS];
struct ferNNParams p2[MAX_CLIENTS];

Mat current_gray[MAX_CLIENTS], current_gray2[MAX_CLIENTS];
BoundingBox pbox[MAX_CLIENTS], pbox2[MAX_CLIENTS];
vector<Point2f> pts1[MAX_CLIENTS], pts12[MAX_CLIENTS];
vector<Point2f> pts2[MAX_CLIENTS], pts22[MAX_CLIENTS];
bool status[MAX_CLIENTS], status2[MAX_CLIENTS];
int frames = 1;
int detections = 1, detections2 = 1;


int main(int argc, char *argv[])
{
	int sockfd, newsockfd, portno;
	socklen_t clilen;
	int to_recv = BUFSIZ;
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
	
	
	//Output file
	FILE  *bb_file = fopen("bounding_boxes.txt","w");
	FILE  *bb_file2 = fopen("bounding_boxes2.txt","w");
	
	for(i = 0; i < MAX_CLIENTS; i++)
	{
		status[i] = true;
		status2[i] = true;
	}

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
						if(strcmp(buffer, "0") == 0)
						{
							send_reply(i);	
							
							n = recv(i, buffer, to_recv, 0);
							receive_tld_params(i, buffer, p1[i], p2[i]);
							//Read parameters file
							tld[i].read(p1[i], p2[i]);
							tld2[i].read(p1[i], p2[i]);
						}
						else if(strcmp(buffer, "1") == 0)
						{
							send_reply(i);
							/**Get Bounding Box information**/
							box[i] = receive_box(i);
							box2[i] = receive_box(i);
							send_reply(i);
						}
						else if(strcmp(buffer, "2") == 0)
						{
							send_reply(i);
							
							/**Get last_gray Mat information**/
							last_gray[i] = receive_mat(i);//Mat(rows, cols, elem_type, matr, step);
							last_gray2[i] = receive_mat(i);
							
#ifdef DEBUG_MAT
							printf("%s\n", last_gray[i].ptr());
#endif
							//TLD initialization
							tld[i].init(last_gray[i], box[i], bb_file);
							tld2[i].init(last_gray2[i], box2[i], bb_file2);
							
							send_reply(i);
						}
						else if(strcmp(buffer, "3") == 0)
						{
							send_reply(i);
							
							frame[i] = receive_mat(i);
							if(frame[i].empty()) break;
							//get frame
							cvtColor(frame[i], current_gray[i], CV_RGB2GRAY);
							cvtColor(frame[i], current_gray2[i], CV_RGB2GRAY);
							//Process Frame
							tld[i].processFrame(last_gray[i],current_gray[i],pts1[i],pts2[i],pbox[i],status[i],tl,bb_file);
#ifdef DEBUG							
							printf("%d %d %d %d %d\n", pbox.x, pbox.y, pbox.width, pbox.height, status);
							printf("%d %d %d %d %d\n", pbox2.x, pbox2.y, pbox2.width, pbox2.height, status2);
#endif
							tld2[i].processFrame(last_gray2[i],current_gray2[i],pts12[i],pts22[i],pbox2[i],status2[i],tl,bb_file2);
							
							printf("%d %d %d %d %d\n", pbox[i].x, pbox[i].y, pbox[i].width, pbox[i].height, status[i]);
							printf("%d %d %d %d %d\n", pbox2[i].x, pbox2[i].y, pbox2[i].width, pbox2[i].height, status2[i]);	
						
							//Send result boxes
							send_box(i, status[i], pbox[i]);
							send_box(i, status2[i], pbox2[i]);
						
							//swap points and images
							swap(last_gray[i],current_gray[i]);
							pts1[i].clear();
							pts2[i].clear();
							swap(last_gray2[i],current_gray2[i]);
							pts12[i].clear();
							pts22[i].clear();
							imshow("TLD", frame[i]);
							//send_reply(i);
							
						}
						if(strcmp(buffer, "4") == 0)
						{
							close(i); 
							FD_CLR(i, &read_fds);
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


