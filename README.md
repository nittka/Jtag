# Jtag

##Scope

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

##Syntax

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
folder_for "Vacation 2017"
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

#Features

* define your own category hierarchies for describing your photos
* hover showing category descriptions (if you provided them)
* content assist
  * keywords
  * categories
* validation (+ quickfixes for some)
  * document not found
  * missing metadata for a file within a folder
* navigation using F3
  * opening the original document
  * navigate to the category definition
* find references `Shift-Ctrl-G`
  * where is the given category used (excluding short cuts or via category hierarchy)
* define searches (invoked with `Alt-X` on the definition); matches are shown in the search view
  * search referenced categories, tags, titles, descriptions
  * boolean operations
  * combine existing (named) searches

##Limitations

For simlicity file names, tags, categories etc. have a restricted character set (a-z, A-Z, digits, undersore and dash). File names containing other characters may be written as strings.