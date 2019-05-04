# Tracker Explorer

## You always been tracked, Why not to track yourself Too

## General Description

In this application you will track yourself, it is a simple file explorer with 2 main views that can interact between each other, And for each file we have a status Seen or Unseen and filtering Media ...And so much more !

This application is not intended to be a replacement to windows explorer but it integrate with it to master Exploring Files.
So Happy Exploring ! [Download here](https://github.com/Ahmad-Said/tracker-explorer/releases/latest)

***

## Sketch Images

![Main](https://i.imgur.com/Bd63Udx.png)

Main View

![Menu](https://i.imgur.com/Z7MCijy.png)

Menu Tracker Options

![VLC Editor](https://i.imgur.com/hlV98fa.png)

VLC Editor

## Features

* File Arguments
  * File Status [seen/unseen]
  * Notes Files [Show on hover/column]
  * Multi Selection Support
  * Filter Video scene exclusion Using VLC Player with smart detection from VLC itself.
* File operations
  * Copy/Move/Paste between views
  * Create/Rename file or directory
  * Reveal in windows Explorer
* Quick Search Field
* Support Resizing and auto Scaling
* Navigation Button (Up/Back/Next)
* Auto expand on click directory to right view on click
* Full keyboard Navigation support
* MultiUser Mode with without restriction
* Windows Context menu support (open Tracker here)
* Recursive Tacker/Cleaner so you don't do things over and over
* Conflict Log State to show New and Deleted/Moved items from last state

***

## Keyboard Shortcuts

| Shortcuts | Action |
| --------- | ------:|
| Navigation| |
| Tab|Focus Table View
|Ctrl + F | Focus on search Field|
|Escape | Clear Search Field|
|Ctrl + Tab | Switch Focus between Tables|
|Alt + Up or BackSpace | Go To parent Directory|
|Alt + Left Arrow | Go Back To Previous Folder|
|Alt + Right Arrow | Go Next|
|Ctrl + Shift + R | Reveal in Windows Explorer|
|Shift + D | Focus On Path Field|
|||
|File Operations on Focus | |
|Space | Toogle MarkSeen|
|Ctrl + N | New File|
|Ctrl + Shift + N | New Directory|
|Ctrl + C | Copy to the other Table|
|Ctrl + X | Move to the other Table|
|Ctrl + X | Delete Selected Files|
|F2| Rename Seleted File|
|||
|Within Table View| |
|F| Focus Search Field|
|S| Clear Search Field|
|Up / Left| Navigate Selected with Shift support|
|Left / Right | Dominate Other Table View|

***

## Tips and Tricks

* Scroll up/down with mouse on clear button to toggle Seen/Unseen search. (or put yes/un)
* Right click on V button to quickly lunch Media withsaved Configuration
* Hold Shift or Ctrl to make various Selection
* Do you know Search also search your notes
* Click on plus mark on table to display note table

  ***

* Advanced
  * Create a temp user, turn off sync by toggling off the '<>' button, set search seen yes on the left and un on the right. Make your files to take, Create a directory in the folder and Select all Seen files(ctrl +A) then Ctrl +X/v to media device to move them and so you did a smart operation selection without changing the structure of folder.

## Specials Thanks to

* [konvio](https://github.com/konvio/javafx-file-manager) for his File Explorer approach using JavaFX.
* [dicolar](https://github.com/dicolar/jbootx) for bootstrap design.