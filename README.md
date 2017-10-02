# Filestore

_Note that this is really new project and perhaps is not stable enough for production use_

This repository contains 2 projects:

* filestore server
* filestore client

## FileStore server

A spring boot based HTTP file storage server which stores files on a disk.

### Features

* REST interface for storing and reading files
* Can download files as attachments or open them in browser (for example image files)
* Multiple "buckets"

### File structure

Files are stored in buckets like this:

```
  bucketA
     fileA.txt
     fileB.txt
     ...
  bucketB
     some_image.png
     other_image.png
     ...
```

Only one level of hierarchy is supported.

### REST operations

#### Querying and downloading

* GET / - get list of bucket
* GET /<bucket-name>/ - get list of files in a bucket
```
[
  {"name": "xyz.png", "mimeType": "image/png", "size": 324567, "dateCreated": "2017-01-01T12:00:00.232"},
  { ... },
  ...
]
```

* GET /<bucket-name>/<file-name> - download a file
* GET /<bucket-name>/<file-name>?att=false - download file not as attachment (effect in browser is to open it)

#### Deleting buckets and files

* DELETE /<bucket-name>/ - delete a bucket (and all files in it)
* DELETE /<bucket-name>/<file-name> - delete a file

#### Creating bucket

POST / - create a bucket, specify bucket name in body

Example:

  ```
  POST /
  
  Request body:
  hello-world
  ```
  
Creates a bucket named hello-world.

#### Uploading new files and overwriting existing ones  

* POST /<bucket-name>/ - upload a file, this takes multipart form file with key file, file will be created with original filename
* POST /<bucket-name>/?filename=xyz.png - upload a file but this time with different filename
* PUT /<bucket-name>/<file-name> - overwrite file with new data
  
### Security

An effort has been made to prevent file access to files outside of content directory but there are no guarantees.
