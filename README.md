# Gospel

## Motivation
This simple tool is used to extract the Vietnamese daily Gospel from https://www.vaticannews.va/vi

This tools will lookup for the Gospel of the day on local file system first, if it exists, then show it.
Otherwise, will parse the HTML from vaticannews and store content on local file system.

## Usage
To build from source, simply run:
```bash
mvn package; java -jar target/gospel-1.0.0-SNAPSHOT.jar
```

## Setup with Native compilation
TODO: implement
