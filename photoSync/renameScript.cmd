@echo off
cls
for /d %d in (\\BOSMANNAS\photo\*) do rename "%d\*.JPG" *.jpg