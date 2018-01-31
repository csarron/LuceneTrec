# Wiki-Lucene

Lightweight Lucene indexing and searching for TREC format data.

## Installation

1. `git clone https://github.com/csarron/Wiki-Lucene.git`

2. `./gradlew b`, then distributable files are in `build/distributions/`

3. `unzip build/distributions/wiki-lucene-0.1.0.zip`

## Usage
    
1. Building indices to `samples_idx` folder using the data from `samples`: 
    
    `wiki-lucene-0.1.0/bin/wiki-lucene index samples samples_idx`

2. Searching 'Anarchism' in `samples_idx`: `wiki-lucene-0.1.0/bin/wiki-lucene search samples_idx 'Anarchism'`
  