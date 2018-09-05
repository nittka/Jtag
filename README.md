# Jtag

## Scope

This is intended to be a lightweight Xtext based photo tagging system. Technologically it is an slightly adapted copy of [Xarchive](https://github.com/nittka/Xarchive). Within a single project you keep 
* a definition file for _categories_
* photos (clips etc.)
* for each folder a `jtag` file containing meta data for photos within this folder (excluding subfolders)

Supported meta data includes
* global information (folder name, file name patterns to ignore, categories and tags that apply to all files within the folder)
* file name
* title
* date
* list of (self defined) categories
* list of tags — additional keywords applying to the document
* description

## Syntax

### definition file

```
categoriesFor status {
  done,
  //a category may have a description (e.g. shown in hover)
  metaDataIncomplete "doc is registered, but meta data has to be completed",
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

categoriesFor place {
  home, away
}

categoriesFor occasion {
  birthday, vacation
}
```

### jtag file

```
//global information
folder "Vacation 2017"
ignore: "*.txt", "ignore.jpg"
occasion: vacation;
tags: Sweden

// start single files
//file name, optional date and optional title
IMG_0011.jpg 2017-02-11 "arrival in Stockholm"
//categories
persons: mom, children;
//tags
tags: Stockholm, hotel, rain
description "late arrival because we first drove to the wrong hotel, everyone exhausted"
.

//most meta data is optional (but without any, tagging makes no real sense)
IMG_0274.jpg "departure".
```

# Features

* define your own category hierarchies for describing your photos
* hover
  * on category shows descriptions (if you provided them)
  * on file name shows image thumbnail
* content assist
  * keywords
  * categories
  * (already used) tags
* validation (+ quickfixes for some)
  * document not found
  * missing metadata for a file within a folder
* navigation using F3
  * opening the original image
  * navigate to the category definition
* find references `Shift-Ctrl-G`
  * where is the given category used (excluding short cuts or via category hierarchy)
* user defined searches (invoked with `Alt-X` on the definition)
  * matches are shown in the search view
  * search referenced categories, tags, titles, descriptions
  * boolean operations
  * combine existing (named) searches
  * resulting images are rendered in external web-browser
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

## Limitations

For simlicity file names, tags, categories etc. have a restricted character set (a-z, A-Z, digits, undersore and dash). File names containing other characters may be written as strings.

## Installation

You need an Eclipse with an Xtext runtime (2.4. or later).
The Jtag update site is [https://www.nittka.de/download/jtag](https://www.nittka.de/download/jtag).
I might provide an Oomph setup file in the future.

metadata-extractor.jar is used under the The Apache Software License, Version 2.0, xmp-core under the BSD License [see](https://www.adobe.com/devnet/xmp/library/eula-xmp-library-java.html).
Both are only used for extracting the date of a picture for the missing description quickfix.