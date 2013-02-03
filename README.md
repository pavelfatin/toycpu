ToyCPU README
=============

This repository holds logic and software CPU emulators that are compatible with
`Hack` computer system from [Nand2Tetris](http://www.nand2tetris.org/) project.

CPU
---

The `Hack.circ` file contains a logic emulator of "Hack" CPU implemented in
[Logisim](http://ozark.hendrix.edu/~burch/logisim/).

The `Convert` utility can be used to convert `.hack` files to Logisim ROM images.

Emulator
--------

The `emulator` directory contains a software emulator of "Hack" computer system consisting of:

* CPU,
* screen,
* keyboard.

This emulator is several orders of magnitude faster than the one supplied with the project
[software suite](http://www.nand2tetris.org/software.php).

In addition to better performance, the emulator offers fine-grained emulation speed control and
smooth graphics rendering, which can be employed to run arcade games.

Pavel Fatin, [http://pavelfatin.com](http://pavelfatin.com/)
