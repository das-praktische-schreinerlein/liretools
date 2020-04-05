# Usage

## create an image-index
- create index
```
bash

# configure index
LIREINDEXER_NUMTHREADS=8
LIREINDEXER_FEATURES=CEDD,FCTH,OpponentHistogram,ColorLayout,JCD,SimpleColorHistogram
W_MYTB_INDEXSRC_MEDIADIR="${W_MYTB_MEDIADIR}pics_x100\\"
W_MYTB_INDEXDIR="F:\\playground\\mytb-test\\mytbindex\\"

# common config
W_LIREOOLS="F:\\Projekte\\liretools\\"

# do it
${LIRETOOLS}/sbin/indexImages.sh "$W_MYTB_INDEXSRC_MEDIADIR" "$W_MYTB_INDEXDIR" "$LIREINDEXER_FEATURES" "$LIREINDEXER_NUMTHREADS"
``` 

## compare any images with index
- do search and check reulting file ```findFilesInLireIndex.json```
```
bash

# confiure dir to search for corresponding images
SEARCHDIR="F:\\playground\\mytb-test\\testimages\\"

# configure searcher
LIRESEARCHER_NUMTHREADS=8
LIRESEARCHER_FEATURES=OpponentHistogram,ColorLayout,SimpleColorHistogram
LIRESEARCHER_MAXDIFFERENCESCORE=8
LIRESEARCHER_SHOWSIMILARHITS=1

# common config
W_MYTB_INDEXDIR="F:\\playground\\mytb-test\\mytbindex\\"
W_LIREOOLS="F:\\Projekte\\liretools\\"

# do it
${LIRETOOLS}/sbin/searchIndexedImages.sh "$SEARCHDIR" "$W_MYTB_INDEXDIR" "$LIRESEARCHER_FEATURES" "$LIRESEARCHER_MAXDIFFERENCESCORE" "${LIRESEARCHER_SHOWSIMILARHITS}" "$LIRESEARCHER_NUMTHREADS"
``` 
- check result-file
```
{  "files": [
    {  "file":     {  "dir": "F:\\playground\\mytb-test\\testsearch",   "name": "257033378_385294.jpg"},
  "records":     [
    {  "id": "D:\\Bilder\\mediadb\\pics_x100\\import-2019-07_20190718-home\\20190718_211011.jpg",   "dir": "D:\\Bilder\\mediadb\\pics_x100\\import-2019-07_20190718-home",   "name": "20190718_211011.jpg",   "matching": "SIMILARITY",   "matchingDetails": "OpponentHistogram",   "matchingScore": "0.34591939901492874"},
    {  "id": "D:\\Bilder\\mediadb\\pics_x100\\import-2019-07_20190718-home\\20190718_211011.jpg",   "dir": "D:\\Bilder\\mediadb\\pics_x100\\import-2019-07_20190718-home",   "name": "20190718_211011.jpg",   "matching": "SIMILARITY",   "matchingDetails": "ColorLayout",   "matchingScore": "2.8284271247461903"}
    ]},
    {  "file":     {  "dir": "F:\\playground\\mytb-test\\testsearch",   "name": "257117455_382159.jpg"},
  "records":     [
    {  "id": "D:\\Bilder\\mediadb\\pics_x100\\import-2019-07_20190718-home\\20190718_211019.jpg",   "dir": "D:\\Bilder\\mediadb\\pics_x100\\import-2019-07_20190718-home",   "name": "20190718_211019.jpg",   "matching": "SIMILARITY",   "matchingDetails": "OpponentHistogram",   "matchingScore": "0.07885363102845805"},
    {  "id": "D:\\Bilder\\mediadb\\pics_x100\\import-2019-07_20190718-home\\20190718_211019.jpg",   "dir": "D:\\Bilder\\mediadb\\pics_x100\\import-2019-07_20190718-home",   "name": "20190718_211019.jpg",   "matching": "SIMILARITY",   "matchingDetails": "SimpleColorHistogram",   "matchingScore": "1.977548284111273"},
    {  "id": "D:\\Bilder\\mediadb\\pics_x100\\import-2019-07_20190718-home\\20190718_211019.jpg",   "dir": "D:\\Bilder\\mediadb\\pics_x100\\import-2019-07_20190718-home",   "name": "20190718_211019.jpg",   "matching": "SIMILARITY",   "matchingDetails": "ColorLayout",   "matchingScore": "4.414213562373095"}
    ]}
  ]
}


```
