/*  Copyright 2011 AIT Austrian Institute of Technology
*
*   This file is part of OpenTLD.
*
*   OpenTLD is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   OpenTLD is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with OpenTLD.  If not, see <http://www.gnu.org/licenses/>.
*
*/
/*
 * gui.cpp
 *
 *  Created on: Oct 18, 2011
 *      Author: clemensk
 */

#include "gui.h"
#include "Main.h"

#include <string>
	using std::string;

namespace tld {

Gui::Gui() :
	m_window_name("tld") {
}

Gui::~Gui() {
}

void Gui::init() {
	cvNamedWindow(m_window_name.c_str(), CV_WINDOW_AUTOSIZE);
	cvMoveWindow(m_window_name.c_str(), 100, 100);
}

void Gui::showImage(IplImage * image) {
	cvShowImage(m_window_name.c_str(), image);
}

char Gui::getKey() {
	return cvWaitKey(10);
}

std::string Gui::windowName() {
	return m_window_name;
}

static string window_name;
static CvFont font;
static IplImage * img0;
static IplImage * img1;
static CvPoint point;
static CvRect * bb;
static int drag = 0;

static void mouseHandler(int event, int x, int y, int flags, void* param) {
	/* user press left button */
	if (event == CV_EVENT_LBUTTONDOWN && !drag) {
		point = cvPoint(x, y);
		drag = 1;
	}

	/* user drag the mouse */
	if (event == CV_EVENT_MOUSEMOVE && drag) {
		img1 = (IplImage *) cvClone(img0);

		cvRectangle(img1, point, cvPoint(x, y), CV_RGB(255, 0, 0), 1, 8, 0);

		cvShowImage(window_name.c_str(), img1);
		cvReleaseImage(&img1);
	}

	/* user release left button */
	if (event == CV_EVENT_LBUTTONUP && drag) {
		*bb = cvRect(point.x, point.y, x - point.x, y - point.y);
		drag = 0;
	}
}

// TODO: member of Gui
// --> problem: callback function mouseHandler as member!
int getBBFromUser(IplImage * img, CvRect & rect, Gui * gui) {
	window_name = gui->windowName();
	img0 = (IplImage *) cvClone(img);
	rect = cvRect(-1, -1, -1, -1);
	bb = &rect;
	bool correctBB = false;
	cvInitFont(&font, CV_FONT_HERSHEY_SIMPLEX, 0.5, 0.5, 0, 1, 8);

	cvSetMouseCallback(window_name.c_str(), mouseHandler, NULL);
	cvPutText(img0, "Draw a bounding box and press Enter", cvPoint(0, 60),
			&font, cvScalar(255, 255, 0));
	cvShowImage(window_name.c_str(), img0);

	while(!correctBB) {
		char key = cvWaitKey(0);
		if(tolower(key) == 'q') {
			return PROGRAM_EXIT;
		}
#ifdef __unix__
		if((key == '\n') && (bb->x != -1) && (bb->y != -1)) {
#endif
#ifdef _WIN32
		if((key == '\r') && (bb->x != -1) && (bb->y != -1)) {
#endif
			correctBB = true;
		}
	}

	cvSetMouseCallback(window_name.c_str(), NULL, NULL);

	cvReleaseImage(&img0);
	cvReleaseImage(&img1);

	return SUCCESS;
}

}
