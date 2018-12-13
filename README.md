# Webserver implemented in Java, that takes youtube links and exposes their audio

Work in Progress:

* can be launched with <code>java -jar dlaudio.war</code>
* can be reached at <code>localhost:8080</code> by default
* creates lib directory on startup if non existent
* downloades videos in a directory and saves audio in another
* directories can't currently be set on startup
* can take a single youtube link
* can take a text file filled with youtube links, seperated by line breaks
* after processing will serve the audios as download (zip-archive with mp3's for text file, mp3 otherwise)
* videos will be deleted after processing
* audios are beeing kept
* backed by ffmpeg to expose audio of videos
* still full of bugs, and unoptimized logic
