//
//  VMParser.h
//  VMTranslator
//
//  Created by Yan Xia on 10/25/12.
//  Copyright (c) 2012 Yan Xia. All rights reserved.
//

#ifndef __VMTranslator__VMParser__
#define __VMTranslator__VMParser__

#include <iostream>
#include <string>
#include <fstream>
#include "VMHelper.h"

using namespace std;

class VMParser {
    string command;
    ifstream sourceVM;
    
public:
    VMParser(string filename);
    ~VMParser();
    bool hasMoreCommand();
    void advance();
    CommandTypes commandType();
};

#endif /* defined(__VMTranslator__VMParser__) */