# Wiki-Lucene

Lightweight Lucene indexing and searching for TREC format data.

## Installation

1. `git clone https://github.com/csarron/LuceneTrec.git`

2. `./gradlew b`, then distributable files are in `build/distributions/`

3. `unzip -o build/distributions/LuceneTrec-0.3.zip`

## Usage
    
1. Building indices to `samples_idx` folder using the data from `samples`: 
    
    `LuceneTrec-0.3/bin/LuceneTrec index samples samples_idx`

2. Searching 'Anarchism' in `samples_idx`: `LuceneTrec-0.3/bin/LuceneTrec search samples_idx 'Anarchism'`


3. `LuceneTrec-0.3/bin/LuceneTrec -h ` for more usage.