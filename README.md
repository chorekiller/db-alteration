# db-alteration

This project aims to be a tool to alter database based on version files.

## Purpose

I (@Isammoc) love the way the [Play framework! evolutions](https://www.playframework.com/documentation/2.8.x/Evolutions)
are played when switching between git branches and I embrace that rollback must lie next to the database alteration.

"alteration" is a loose synonym for evolution and most commands will be `ALTER TABLE`. So it makes sense to me.

## Repository architecture
This repository is split in modules:

 * :construction_worker_man: `core`: only the library with as few dependencies as possible
 * :construction_worker_man: `test`: a test library for your own alteration
 * :construction_worker_man: `cli`: to be able to launch the alterations from the cli (fat jar distribution, or binary if I can manage the graal)
 * :construction_worker_man: `http4s`: to provide a middleware managing the alteration
 * :construction_worker_man: `sbt-db-alteration`: to play alteration from sbt
 * :construction_worker_man: `sample`: containing different sample projects

## Getting started

TBD

## Licensing

TBD I like the WTF license, but perhaps I'll change my mind.

## Acknowledgement

The principles under db-alteration come mostly from
[Play framework! db-evolutions module](https://www.playframework.com/documentation/2.8.x/Evolutions).
I (@Isammoc) love the way the evolution are played when switching between branches and I embrace that rollback must lie
next to the database alteration.
