# Filestore

_WARNING: In early stage of development_

This repository contains 2 projects:

* filestore server
* filestore client

## FileStore server

A spring boot based HTTP file server with files stored on a local filesystem.

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

Only one level of hierarchy is supported ie. folders in buckets are not supported.

Current implementation supports only local filesystem with buckets as directories in configured path and files stored in bucket directories.

### Building, installing and running

To run tests:
```
$ gradle test
```

To create a distribution zip (build, compress)
```
$ gradle distribution
```

The zip file is located under _server/build/dist/filestore-server-1.0.zip_. 

To run the service extract zip file into desired directory and from that directory edit application.properties file to your liking (most notably set filestore.contentDir property) and then run
```
$ java -jar filestore-server-1.0.jar
```

This will start an HTTP server at http://localhost:8080 if you did not change defaults. Files/buckets will be served from
configured _contentDir_ (by default ./content).


### Configuration

### REST operations

All REST operations will return 200 on success with no response body unless specified otherwise.

All error responses (>= 400) will have a response body which looks like this:

```
{
  "error": true,
  "message": "Some description .... could be null"
}
```

#### Querying and downloading

##### GET /store/

Get list of buckets. 

Returns array of bucket names on success (status code 200):

```
[ "bucketA", "bucketB", "bucketC", ... ]
```

##### GET /store/bucket-name/

Get list of files in a bucket.

Returns response looking like this on success (status code 200):
```
[
  {"name": "xyz.png", "mimeType": "image/png", "size": 324567, "dateCreated": "2017-01-01T12:00:00.232"},
  { ... },
  ...
]
```

Error responses: 

* 404 if bucket does not exists
* 400 if bucket name is invalid.

##### GET /store/bucket-name/file-name

Download a file.

Returns file data on success (status code 200).

Error responses:

* 404 if bucket or file do not exist
* 400 if bucket name or file name is invalid

##### GET /store/bucket-name/file-name?att=false

Download file not as attachment (effect in a browser is to open file in current tab).

Returns 200 on success together with file data.

Error responses:

* 404 if bucket or file do not exist
* 400 if bucket name or file name is invalid

#### Deleting buckets and files
 
##### DELETE /store/bucket-name/

Delete a bucket (and all files in it, use with care).

Returns 200 on success

Error responses:

* 404 if bucket or file do not exist
* 400 if bucket name or file name is invalid

##### DELETE /store/bucket-name/file-name

Delete a file.

Returns 200 on success

Error responses:

* 404 if bucket or file do not exist
* 400 if bucket name or file name is invalid

#### Creating bucket

##### POST /store/

Create a bucket, specify bucket name in body

Example:

  ```
  POST /store/
  
  Request body:
  hello-world
  ```
  
Creates a bucket named hello-world.

Error responses:

* 409 if bucket with same name already exist
* 400 if bucket name invalid

#### Uploading new files and overwriting existing ones  

##### POST /store/bucket-name/

Upload a file, this takes multipart form file with key file, file will be created with original filename

Error responses:

* 409 if file already exists
* 404 if bucket does not exist
* 400 if bucket or file name invalid

##### POST /store/bucket-name/?filename=xyz.png

Upload a file but with different filename

Error responses:

* 409 if file already exists
* 404 if bucket does not exist
* 400 if bucket or file name invalid

Uploading can not overwrite existing file, if you attempt to upload to an existing file you will get 409. To overwrite existing file use:

##### PUT /store/bucket-name/file-name

Overwrite file with new data.

Error responses:

* 404 if file does not exists
* 404 if bucket does not exist
* 400 if bucket or file name invalid
  
### Security

An effort has been made to prevent file access outside of but I can not give full guarantee. This is accomplished by
validating _bucket-name_ and _file-name_ in each request, only certain patterns are allowed which should prevent
potential attacks.

At the moment there is no any kind of security integration (OAuth, spring security, ...) this is planned for the future.

### Actuator

Spring actuator is available at /mngmnt path.

At the moment only /mgmnt/health and /mgmnt/info are accessible from outside.
