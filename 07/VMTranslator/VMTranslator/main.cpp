//
//  main.cpp
//  VMTranslator
//
//  Created by Yan Xia on 10/25/12.
//  Copyright (c) 2012 Yan Xia. All rights reserved.
//

#include <iostream>
#include "VMTranslate.h"
#include "VMParser.h"

int main(int argc, const char * argv[])
{
    VMTranslate *translate = new VMTranslate();
    translate->start();
    return 0;
}

