//
//  VMTranslate.h
//  VMTranslator
//
//  Created by Yan Xia on 10/25/12.
//  Copyright (c) 2012 Yan Xia. All rights reserved.
//

#ifndef __VMTranslator__VMTranslate__
#define __VMTranslator__VMTranslate__

#include <iostream>
#include <fstream>

using namespace std;

class VMTranslate {
    ofstream output;
    
public:
    VMTranslate();
    ~VMTranslate();
    void start() {return;}
};

#endif /* defined(__VMTranslator__VMTranslate__) */
