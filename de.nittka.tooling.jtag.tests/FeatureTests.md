# Features to test

This is a list of features for which tests would be nice.
In the long run automated tests would be nice, but...
Most of the following features can be tested using the JtagTestProject.
Due to few images and none with gps data, the "Open preview in browser" and "Show On Map" features cannot be well tested.

## Editor
* Formatting
  * Category definitions
  * Searches
  * Jtag file
* Code Completion
  * Category types (+template)
  * Categories
  * Tags
    * in file description (+template)
    * in search
  * ignore in folder
    * (only) missing file
    * file patterns
  * category and description template in file
  * date from exif
  * file name when description is missing
* Validation
  * at most one configuration file
  * duplicate category type
    * in File description
    * in category definitions
  * duplicate category
    * in type definition (also different levels)
    * between type definitions (warning)
    * within File description
  * date format in file description
  * described file exists
  * all files in file have description (ignore pattern matches) excluded)
  * folder ignore pattern does not cover a missing file (warning)
  * names for searches unique
    * within file
    * between files
  * no ignore patterns in named search (warning)
  * unused tag in search (warning)
  * search date
    * basic syntax
    * format (wildcard patterns)
    * invalid date
    * illegal interval
* Quickfix
  * add entries for missing files (ignor patterns!)
* Hover
  * category type description
  * category description
  * file image
* Labels an images in
  * Outline (all file types)
  * search result (find references and Jtag search)
* Go to declaration (F3)
  * on category type (file/search)
  * on category (file/search)
  * on search reference
  * on file name (open extern)
* Find references
  * on category type (file/search/definition)
  * on category (file/search/definition)
  * on search (name)
* folding
  * fold on open preference
* show on Map

## Jtag search
* not enabled in definition/jtag file
* no search executed if error in definition (also in reused named search, syntax errors in file/other searches)
* simple text search (file name, title, description)
* simple category search
* simple tag search
* date searches
  * exact date (yyyy-MM-dd, yyyy-MM-?, yyyy-?-?, ?-MM-dd, ?-MM-?)
  * from date
  * to date
  * interval
  * inverse interval
  * combinded searches
  * reuse named searches
  * folder ignore patterns
* html preview opens
* gps html opens
* number of matches shown in search view label

## Jtag Image View
show image
* navigator single file selection
* jtag folder outline single selection
* jtag Jtag search result single selection
* find references search result single selection
* jtag editor single file selected

## Jtag Navigator
* new Jtag file and search wizard
  * directly visible under "New"
  * proposed name
  * name validation (empty, file extension, file already exists, no white spaces)
  * warning if jtag file already exists (not for root folder)
  * initial content (file definitions including dates / initial search)
* show on Map action
* image view reacts to selection change
* Filter
  * non-jtag files hidden if activated
* Sorting
  * sort jtag files first, if activated
* prevent move/delete of non-jtag files (+preference)

## Further features
* copy used tags (all tags and count copied to clipboard)
* in Jtag perspective Jtag Navigator and Image view are directly visible under "Window show View"
* Show on map (not enabled in search or definition jtag)
* maximize Image view with shift-ctrl-m (from editor, navigator, outline, search result)
* help pointing to github wiki
