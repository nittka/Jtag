# Jtag

## Scope

Jtag is a lightweight Xtext based photo tagging system. Technologically, it is an adapted copy of [Xarchive](https://github.com/nittka/Xarchive).
Within a single project you keep
* a definition file for _categories_ (one file in the project root)
* folders with photos, clips etc. 
* for each folder a `jtag` file containing meta data for photos within this folder (excluding subfolders)
* one or more files with searches

Supported meta data includes
* global information (folder name, categories and tags that apply to all files within the folder)
* file name
* title
* date
* list of (self defined) categories
* list of tags — additional keywords applying to the document
* description

## Syntax

### definition file

```
categoriesFor status "optional description of category type" {
  done,
  //a category may have a description (e.g. shown in hover)
  metaDataIncomplete "image is registered, but meta data has to be completed",
  //a category may have sub categories
  todo {
    titleMissing,
    categoriesIncomplete
  }
}

categoriesFor person {
  parents {
    mom, dad
  },
  children {
    daughter, son
  }
}

categoriesFor occasion {
  birthday, vacation
}
```

### jtag file

```
//global information
folder "Vacation 2017"
//optional list of file patterns ignored when checking for missing entries
ignore: "*.txt", "ignore.jpg"
occasion: vacation;
tags: Sweden

// start single files
//file name, date and short title
IMG_0011.jpg 2017-02-11 "arrival in Stockholm"
//categories
persons: mom, children;
//tags
tags: Stockholm, hotel, rain
//longer description
description "late arrival because we first drove to the wrong hotel, everyone exhausted"
.

//most meta data is optional (but without any, tagging makes no real sense)
IMG_0274.jpg "departure".
```

### search file

```
//search in file name, title and description
search text "arrival" 

//combine search criteria
search category persons:son && date from 2016-?-? to 2017-06-?
```

## Features

* define your own category hierarchies for describing your photos
* hover
  * on category shows descriptions (if provided)
  * on image file name shows thumbnail
* content assist
  * keywords
  * categories
  * (already used) tags
  * files to ignore
* validation (+ quickfixes for some)
  * document not found
  * missing metadata for a file within a folder
  * duplicate names
  * date search patterns
* navigation using F3
  * opening the original file in an external editor
  * navigate to the category definition
* find references `Shift-Ctrl-G`
  * where is the given category used (excluding via category hierarchy)
* user defined searches (invoked with `Alt-X` on the search definition)
  * search categories, tags, text, dates
  * matches are shown in the search view
  * resulting images can be rendered in an external web-browser (if GPS data and internet connection are present also on a map)
  * boolean operations
  * combine existing (named) searches
* view showing the image currently selected in
  * navigator
  * editor
  * outline
  * search view
* Jtag perspective
  * default views (navigator, image preview, problems/outliene)
  * navigator with quick access to new jtag file wizard
  * optional navigator filter (show only jtag files)
  * navigator sorting (jtag file before any other files)
  * show selected images on a map (if GPS data is present)

## Limitations

All files within must be contained in one project.
For simlicity file names, tags, categories etc. have a restricted character set (a-z, A-Z, digits, undersore and dash).
File names containing other characters may be written in double quotes.

## Installation

You need an Eclipse with an Xtext runtime (2.4. or later).
If you use the Eclipse installer (Oomph) you can use the following project setup URL: [https://raw.githubusercontent.com/nittka/Jtag/master/JtagUser.setup](https://raw.githubusercontent.com/nittka/Jtag/master/JtagUser.setup).
The Jtag update site is [https://www.nittka.de/download/jtag](https://www.nittka.de/download/jtag).

metadata-extractor.jar is used under the The Apache Software License, Version 2.0, xmp-core under the BSD License [see](https://www.adobe.com/devnet/xmp/library/eula-xmp-library-java.html).
Both are only used for extracting the date of a picture for the missing description quickfix and for extracting GPS data.

## Documentation

For more detailed information on installation, features etc. see the [project wiki](https://github.com/nittka/Jtag/wiki).