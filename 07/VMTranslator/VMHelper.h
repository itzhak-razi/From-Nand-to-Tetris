//
//  VMHelper.h
//  VMTranslator
//
//  Created by Yan Xia on 10/25/12.
//  Copyright (c) 2012 Yan Xia. All rights reserved.
//

#include <iostream>
#include <string>
#include <vector>
#include <sstream>

const std::string whiteSpaces( " \f\n\r\t\v" );


void trimRight( std::string& str,
               const std::string& trimChars = whiteSpaces );


void trimLeft( std::string &str,
              const std::string &trimChars = whiteSpaces );


void trim( std::string& str, const std::string& trimChars = whiteSpaces );

std::vector<std::string>& split(const std::string &s, char delim, std::vector<std::string> &elems);


std::vector<std::string> split(const std::string &s, char delim);

enum CommandTypes {
    C_ARITHMETIC = 0,
    C_MEMORY = 1
};