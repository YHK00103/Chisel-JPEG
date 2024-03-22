# Chisel Jpeg
This project implements Jpeg compression for raw pixel data values.
More information can be found in our presentation: https://docs.google.com/presentation/d/1uGHCBY0pUD06bBndzRFlM-fH3voTCw0arw5I-TWHsZk/edit?usp=sharing
## Jpeg Compression
JPEG compression attempts to create patterns in the color values in order to reduce the amount of data that needs to be recorded, thereby reducing the file size.

To achieve this, we'll follow the outlined approach below. Initially, we'll apply the Discrete Cosine Transform (DCT), followed by quantization of the DCT coefficients. Subsequently, we'll perform zig-zag parsing and then reduce the resulting 2D array into a 1D array using either run-length encoding or delta encoding. Finally, we'll apply either Huffman or arithmetic encoding to complete the compression process.

![Jpeg Compression](https://github.com/Darren-lin/Chisel-JPEG/blob/main/resources/JPEGCompressionOverview.png)

### Usage
<p><b>
Please note that the Discrete Cosine Tests and the JPEGEncodeChiselTest takes a long time to complete.
For this reason we commented out optional additional tests (see line 109 in jpegTester.scala) for JPEGEncodeChiselTest, but they do all run fine and pass.
</b></p>
You can run the included test with:
```sh
sbt test
```

You should see a whole bunch of output that ends with something like the following lines
```
[info] Run completed in 4 minutes, 35 seconds.
[info] Total number of tests run: 88
[info] Suites: completed 11, aborted 0
[info] Tests: succeeded 88, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 276 s (04:36), completed Mar 21, 2024, 11:45:40 PM
```
## What are we currently working on?
 - [ ] Multi-Cycle computation for Discrete Cosine Transform
 - [ ] Fix Quantizatiuon rounding bug

## TODO
 - [ ] Inversing Discrete Cosine Transform
 - [ ] JPEG Decompression in Top Level Module
 - [ ] Reformat ZigZag to remove redundant code



## Known Issues
- [ ] Quantization has a small bug that will round remainder results of -1.49 to -2 when it should be -1 because Quantization rounds to the nearest integer.

## Progress
- [ ] Jpeg Compression
    - [x] Model
    - [x] Model test
    - [x] Hardware
         - [x] Top module
         - [x] Top modle test
    - [x] Hardware Encoding
         - [x] Discrete Cosine Transform (DCT)
         - [x] DCT Tests
         - [x] Zig Zag Parsing
         - [x] Zig Zag Parsing Tests
         - [x] Run length Encoding (RLE)
         - [x] RLE Encoding Tests
         - [x] Delta Encoding
         - [x] Delta Encoding Tests
         - [x] Quantization
         - [x] Quantization Encoding Tests
    - [ ] Hardware Decoding
         - [ ] Discrete Cosine Transform (DCT)
         - [ ] Inverse DCT Tests
         - [x] Zig Zag Reverse
         - [x] Zig Zag Reverse Tests
         - [x] Run length Decoding (RLE)
         - [x] RLE Decoding Tests
         - [x] Delta Decoding
         - [x] Delta Decoding Tests
         - [x] Quantization
         - [x] Quantization Decoding Tests
         

