//
//  VMHelper.cpp
//  VMTranslator
//
//  Created by Yan Xia on 10/25/12.
//  Copyright (c) 2012 Yan Xia. All rights reserved.
//

#include "VMHelper.h"

void trimRight( std::string& str,
               const std::string &trimChars)
{
    std::string::size_type pos = str.find_last_not_of( trimChars );
    str.erase( pos + 1 );
}


void trimLeft( std::string& str,
              const std::string& trimChars)
{
    std::string::size_type pos = str.find_first_not_of( trimChars );
    str.erase( 0, pos );
}


void trim( std::string &str, const std::string &trimChars)
{
    trimRight( str, trimChars );
    trimLeft( str, trimChars );
}

std::vector<std::string> &split(const std::string &s, char delim, std::vector<std::string> &elems) {
    std::stringstream ss(s);
    std::string item;
    while(std::getline(ss, item, delim)) {
        elems.push_back(item);
    }
    return elems;
}


std::vector<std::string> split(const std::string &s, char delim) {
    std::vector<std::string> elems;
    return split(s, delim, elems);
}