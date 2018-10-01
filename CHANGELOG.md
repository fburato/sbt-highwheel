# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/).

## [1.5] - 2018-10-01

The world is clearly better with 1.5. New core `highwheel-modules` (now at version `1.3.0`) means more analysis with one pass of the bytecode, which means more efficiency and more fun.

### Changed

- `highwheelSpecFile` option renamed to `highwheelSpecFile` and changed its type to a sequence to allow multiple specification in the analysis.
- `highwheelEvidenceLimit` type change to `Option[Int]` to make the evidence limitation more semantically close to the configuration.

## [1.4] - 2018-04-16

Listing all pieces of evidence about a dependency violation seems like a good idea until you realise that even for small projects these pieces multiply quickly. 
Too quickly. Like, rabbit multiplication rate quickly. This doesn't happen anymore and you can now set how many rabbits you want reported for every violation with the setting `highwheelEvidenceLimit` setting.

### Added
- `highwheelEvidenceLimit` allows to limit how many pieces of evidence per violation the analysis will report. Set it to 0 to remove evidences from the ~~crime scene~~ analysis results.

## [1.3] - 2018-04-15

What is better than a error that forces you to release two times in one day the same project? An error the forces you to release two times in one day two projects!
The semantic I put into the path logic clearly didn't work. So I have to release 1.3 to use the latest algorithm in `highwheel-modules-core`

### Changed
- The evidence reporting now provides all access points that lead to a dependency violation instead of one constrained path.

## [1.2] - 2018-04-15

With `highwheel-modules` 1.1.0 comes the new world of evidence of the dependency violation. Leaving the sbt plugin behind would be less than optimal so a new evidence reporting feature has been added.

### Changed
- The plugin now reports a path that leads to the dependency violation.

## [1.1] - 2018-04-14

With the restructuring of the highwheel-modules project under my direct supervision important and vital things, like package names, needed to be changed. Kneel before your new master!

### Changed
- The plugin depends now on `com.github.fburato:highwheel-modules-core:1.0.0` recently released.

## [1.0] - 2018-03-25

Initial implementation of the plugin. Java is not really the best JVM language. Scala is. 
Why would I not have an SBT plugin to run highwheel-modules? No reason at all! `sbt-highwheel` fills that hole in your life that you didn't even know existed.

This initial release includes still references to the version of the plugin based on the fork of [Henry's project](https://github.com/hcoles/highwheel) which we lastly decided to separate entirely from the modules analysis functionalities.
