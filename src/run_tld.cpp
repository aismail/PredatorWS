#include <opencv2/opencv.hpp>
#include <tld_utils.h>
#include <iostream>
#include <sstream>
#include <TLD.h>
#include <stdio.h>
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

void readBB(char* file){
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
void mouseHandler(int event, int x, int y, int flags, void *param){
	Rect *pbox;
  switch( event ){
  case CV_EVENT_MOUSEMOVE:
    if (drawing_box){
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
    if( pbox->width < 0 ){
        pbox->x += pbox->width;
        pbox->width *= -1;
    }
    if( pbox->height < 0 ){
        pbox->y += pbox->height;
        pbox->height *= -1;
    }
    if(hasSecondBox)
	    gotBB = true;
    break;
  }
}

void print_help(char** argv){
  printf("use:\n     %s -p /path/parameters.yml\n",argv[0]);
  printf("-s    source video\n-b        bounding box file\n-tl  track and learn\n-r     repeat\n");
}

void read_options(int argc, char** argv,VideoCapture& capture,FileStorage &fs){
  for (int i=0;i<argc;i++){
      if (strcmp(argv[i],"-b")==0){
          if (argc>i){
              readBB(argv[i+1]);
              gotBB = true;
          }
          else
            print_help(argv);
      }
      if (strcmp(argv[i],"-s")==0){
          if (argc>i){
              video = string(argv[i+1]);
              capture.open(video);
              fromfile = true;
          }
          else
            print_help(argv);

      }
      if (strcmp(argv[i],"-p")==0){
          if (argc>i){
              fs.open(argv[i+1], FileStorage::READ);
          }
          else
            print_help(argv);
      }
      if (strcmp(argv[i],"-tl")==0){
          tl = true;
      }
      if (strcmp(argv[i],"-r")==0){
          rep = true;
      }
  }
}

int main(int argc, char * argv[]){
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
  if (fromfile){
      capture >> frame;
      cvtColor(frame, last_gray, CV_RGB2GRAY);
      cvtColor(frame, last_gray2, CV_RGB2GRAY);
      frame.copyTo(first);
  }else{
      capture.set(CV_CAP_PROP_FRAME_WIDTH,340);
      capture.set(CV_CAP_PROP_FRAME_HEIGHT,240);
  }

  ///Initialization
GETBOUNDINGBOX:
  while(!gotBB)
  {
    if (!fromfile){
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
	  if (min(box2.width,box2.height)<(int)fs.getFirstTopLevelNode()["min_win"]){
		  cout << "Bounding box too small, try again." << endl;
		  hasSecondBox = false;
		  gotBB = false;
		  goto GETBOUNDINGBOX;
	  }
	}
	else
  if (min(box.width,box.height)<(int)fs.getFirstTopLevelNode()["min_win"]){
      cout << "Bounding box too small, try again." << endl;
      hasFirstBox = false;
      gotBB = false;
      goto GETBOUNDINGBOX;
  }
  //Remove callback
  cvSetMouseCallback( "TLD", NULL, NULL );
  printf("Initial Bounding Box = x:%d y:%d h:%d w:%d\n",box.x,box.y,box.width,box.height);
  printf("Initial Bounding Box = x:%d y:%d h:%d w:%d\n",box2.x,box2.y,box2.width,box2.height);
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
  while(capture.read(frame)){
    //get frame
    cvtColor(frame, current_gray, CV_RGB2GRAY);
    cvtColor(frame, current_gray2, CV_RGB2GRAY);
    //Process Frame
    tld.processFrame(last_gray,current_gray,pts1,pts2,pbox,status,tl,bb_file);
    tld2.processFrame(last_gray2,current_gray2,pts12,pts22,pbox2,status2,tl,bb_file2);
    //Draw Points
    if (status){
      drawPoints(frame,pts1);
      drawPoints(frame,pts2,Scalar(0,255,0));
      drawBox(frame,pbox);
      detections++;
    }
    if (status2){
      drawPoints(frame,pts12);
      drawPoints(frame,pts22,Scalar(0,255,0));
      drawBox(frame,pbox2);
      detections2++;
    }
    //Display
    imshow("TLD", frame);
    //swap points and images
    swap(last_gray,current_gray);
    pts1.clear();
    pts2.clear();
    swap(last_gray2,current_gray2);
    pts12.clear();
    pts22.clear();
    frames++;
    printf("Detection rate: %d/%d\n",detections,frames);
    printf("Detection2 rate: %d/%d\n",detections2,frames);
    if (cvWaitKey(33) == 'q')
      break;
      printf("---------------------------------------------------------\n");
  }
  if (rep){
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
