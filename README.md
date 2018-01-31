# Wiki-Lucene

Lightweight Lucene indexing and searching for TREC format data.

## Installation

1. `git clone https://github.com/csarron/LuceneTrecEnWiki.git`

2. `./gradlew b`, then distributable files are in `build/distributions/`

3. `unzip -o build/distributions/LuceneTrecEnWiki-0.2.zip`

## Usage
    
1. Building indices to `samples_idx` folder using the data from `samples`: 
    
    `LuceneTrecEnWiki-0.2/bin/LuceneTrecEnWiki index samples samples_idx`

2. Searching 'Anarchism' in `samples_idx`: `LuceneTrecEnWiki-0.2/bin/LuceneTrecEnWiki search samples_idx 'Anarchism'`
  