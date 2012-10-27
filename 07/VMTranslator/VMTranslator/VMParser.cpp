//
//  VMParser.cpp
//  VMTranslator
//
//  Created by Yan Xia on 10/25/12.
//  Copyright (c) 2012 Yan Xia. All rights reserved.
//

#include "VMParser.h"

VMParser::VMParser(string filename) {
    sourceVM.open(filename);
}

VMParser::~VMParser() { }

bool VMParser::hasMoreCommand() {
    if (sourceVM.good() && !sourceVM.eof()) {
        return true;
    } else {
        sourceVM.close();
        return false;
    }
}

void VMParser::advance() {
    do {
        getline(sourceVM, command);
    } while (command != "" &&
             command.find("//") == -1);
    trim(command);
    vector<string> tokens = split(command, ' ');
    string finalCommand;
    for (vector<string>::iterator it = tokens.begin(); it != tokens.end(); ++it) {
        finalCommand += *it;
    }
    command = finalCommand;
}

CommandTypes VMParser::commandType() {
    return C_ARITHMETIC;
}