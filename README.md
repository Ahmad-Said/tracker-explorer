# Tracker Explorer

## You've always been tracked, why not track yourself Too!?

### Table of Contents

* [General Description](#general-description)
* [Sketch Images](#sketch-images)
* [Features](#features)
* [Search Usage](#search-usage)
* [Keyboard Shortcuts](#keyboard-shortcuts)  
* [Fight The New Drug](#fight-the-new-drug)
* [Quick Workflow for VLC Filtering Video](#fight-the-new-drug)
  * [Making Exclusion](#fight-the-new-drug)
  * [Exporting Result](#exporting-result-generation)
* [Tips and Tricks](#tips-and-tricks)
* [Release Notes](#release-notes)

## General Description

In this application you will track yourself, it is a simple file explorer with 2 main views that can interact between each other, And for each file, we have a status Seen or Unseen and filtering Media ...And so much more!

This application is not intended to be a replacement to windows explorer but it integrates with it to master Exploring Files.
So Happy Exploring! [Download here](https://github.com/Ahmad-Said/tracker-explorer/releases/latest)

***

## Sketch Images

Main View

![Main](https://i.imgur.com/g4DADGi.png)

Menu Tracker Options

![Menu](https://i.imgur.com/kPYkr29.png)

Rename Utility

![Rename](https://i.imgur.com/u14ncbx.png)

Photo Explorer

![Photo](https://i.imgur.com/mECgVA8.png)

## Features

* File Arguments
  * File Status [seen/unseen]
  * Notes Files [Show on hover/column]
  * Multi Selection Support
  * Filter Video scene exclusion Using VLC Player with smart detection from VLC itself.
* File operations
  * Copy/Move/Paste between views
  * Create/Rename file or directory
  * Reveal in Windows Explorer
* Quick Search Field
* Support Resizing and Auto Scaling
* Navigation Button (Up/Back/Next)
* Auto expand on click directory to right view on click
* Full keyboard Navigation support
* MultiUser Mode with without restriction
* Windows Context menu upon right click
* Recursive Tacker/Cleaner so you don't do things over and over
* Recursive view so you see all files and folder all together in no time!
* Conflict Log State to show New and Deleted/Moved items from the last state

***

## Search Usage

The search will filter the current view in no time since all files are indexed!
Combine this usage with recursive search.  
Note: you can right click on 'clear' button so it roll showing these options also, and double click it to clear form.

| Operation Key| Action |
| :----: | :-----:|
| ; | combine multiple search statement (and) |
| ;! | exclude from search (not) |
| ;\| | make another search ignoring previous (or) |
| < | operator (<,>,=) to make comparison with numbers within text |
|||
|Reserved keywords | |
| vlc | show all media supported by vlc |
| audio | show all audio |
| video |  show all video |
| image |  show all images |

```bash
Example showing all vlc media that contain name word and not excel:
            'vlc;word;!excel'
```

***

## Keyboard Shortcuts

| Shortcuts | Action |
| --------- | ------:|
| Navigation| |
| Tab|Focus Table View
|Ctrl + F | Focus on search Field|
|Escape | Clear Search Field|
|Ctrl + Tab or F3 | Switch Focus between Tables|
|Alt + Up or BackSpace | Go To parent Directory|
|Alt + Left Arrow | Go Back To Previous Folder|
|Alt + Right Arrow | Go Next|
|Alt + Shift + R | Reveal in Windows Explorer|
|Shift + D | Focus On Path Field|
|Shift + F | Mark Folder As Favorite|
|||
|File Operations on Focus | |
|Space | Toggle MarkSeen|
|Ctrl + N | New File|
|Ctrl + Shift + N | New Directory|
|Ctrl + C | Copy to the other Table|
|Ctrl + X | Move to the other Table|
|Del | Delete Selected Files|
|F2| Rename Selected File|
|||
|Within Table View| |
|F| Focus Search Field|
|S| Clear Search Field|
|Up / Left| Navigate Selected with Shift support|
|Left / Right | Dominate Other Table View|

***

## [Fight The New Drug](https://fightthenewdrug.org/)

### Your Brain was Made To take Care of you, So take care of it! [Click Here](https://fightthenewdrug.org/)

### Quick Workflow for VLC Filtering Video

![VLC Editor](https://i.imgur.com/xaGuDPc.png)

Let's say you have a great movie that conation few bad scene, and you want
to watch the movie with your friends or family, you have to be aware every
minutes while watching it - to pause the movie and skip these scene.
Now with filtering video with this application you can set the begin and 
the end of the scene to skip, to create a playlist file and when watching
the movie using this file, it will auto skip scene and you can watch 
and enjoy the movie at ease.

TO DO List:

1. Click on 'V' button next to media file to open control windows
2. hit the button pick start
     * A VLC instance will start the media
     * Go to start scene to exclude
     * close VLC.
3. hit pick end Button and do the same
4. Add Description (optionally):
    * Why you exclude the scene
    * Summary of events happened
5. hit the button Exclude this
6. Repeat The process from 2. until you've done.

### Exporting Result (Generation)

After Creating Exclusion of the media you could do any of the following:

* Hit Run button at any row to start the media from the current scene and above
* Hit Save button to save exclusion information to data tracker.
  * Exit editor windows
  * right click on V button to start the media respecting rules created.
* Generate XSPF File:
  * will create a .xspf file next to media file with the same name
  * starting this xspf file from explorer will start media following the rule independently from the application.
* Copy raw Data will copy to clipboard the exclusion information in the table
  * Pasting these data in any media file using paste button will automatically Detect configurations and add them to the table.

### Running

When running a filtered media using this tool, you can control the backward of media using playlist control in vlc (press ctrl+L), or using any remote tool check vlc menu. And here where notify-end features become useful denoting the start and the end of each scene  
Other wise the forward of the media is supported using normal controls.
Be sure not turning on Random Switch in VLC toolbar to guarantee playing
scene in order.

## Tips and Tricks

* Scroll up/down with mouse on the clear button to toggle Seen/Unseen search. (or put yes/un)
* Right-click on V button to quickly lunch Media with saved Configuration
* Hold Shift or Ctrl to make various Selection
* Do you know Search also search your notes
* Click on the plus mark on the table header to display note column
* Quick copy note: make a selection -> then click on source note button -> ok! you're done
  ***

* Advanced
  * Create a temp user, turn off sync by toggling off the '<>' button, set search seen yes on the left and un on the right. Make your files to take, Create a directory in the folder and Select all Seen files(Ctrl +A) then Ctrl +X/v to media device to move them and so you did a smart operation selection without changing the structure of folder.

### Release Notes

[Click Here For Full log](ReleasesNotes.txt)