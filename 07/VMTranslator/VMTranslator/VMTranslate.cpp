//
//  VMTranslate.cpp
//  VMTranslator
//
//  Created by Yan Xia on 10/25/12.
//  Copyright (c) 2012 Yan Xia. All rights reserved.
//

#include "VMTranslate.h"

VMTranslate::VMTranslate() {
    output.open("output.asm");
}

VMTranslate::~VMTranslate() { output.close(); }