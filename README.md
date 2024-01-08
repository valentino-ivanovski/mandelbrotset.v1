# Mandelbrot Set

This project, developed by Valentino Ivanovski, was created as part of the Computer Science course Programming III at UP FAMNIT in 2023. The instructions below will guide you on how to open the program in IntelliJ IDEA. Note that the distributive mode is incomplete and intended to run only within the IDE.

## Download link (MacOS): [Mandelbrot Set](https://dropover.cloud/7c88be)

## Setup JavaFX (Instructions for IntelliJ IDEA)

1. Download the "javafx-sdk-19" folder available in this repository.
2. Open the project in IntelliJ IDEA and navigate to File → Project Structure.
3. Click on Libraries and then on the plus button at the top left.
4. Select Java and locate the "javafx-sdk-19" folder. Open it and go to "lib." Look for a file named javafx.swing and add it as a library.
5. Add the entire "lib" folder as a library using the same process as above.
6. The program should be ready to run.

## Setup MPJ (Distributive in multicore mode)

1. Download MPJ Express.
2. Import mpj.jar to the "untitled" project.
3. Set an `$MPJ_HOME` environmental variable to the MPJ Express folder.
4. Modify the path to the "src" folder of the "untitled" project on your machine in line 767 of `myMandelbrot.java`.
5. If running on Windows, change the "/" to "\" in paths.

## Program Preview:

<p align="center">
  <img src="./Images/SS1.png" alt="Program Screenshot">
</p>

## How the Program Works:

<p align="center">
  <img src="./Images/GIF1.gif" alt="Program GIF" style="max-width:100%; width:200%">
</p>
