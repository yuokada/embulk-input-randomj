# Randomj input plugin for Embulk

Embulk plugin for generate dummy records by Java.

Original: [kumagi/embulk\-input\-random](https://github.com/kumagi/embulk-input-random)

## Overview

* **Plugin type**: input
* **Resume supported**: no
* **Cleanup supported**: no
* **Guess supported**: no

## Install 

``` shell
% embulk gem install embulk-input-randomj
```

## Configuration

- **rows**: Generate rows per thread (integer, required)
- **threads**: Thread number (integer, default: `1`)
- **primary_key**: Sequential ID column (string, default: `"""`)
- **schema**: Schema infomation (list, required)

## Example

```yaml
in:
    type: randomj
    rows: 16
    threads: 1
    primary_key: myid
    schema:
      - {name: myid,     type: long}
      - {name: named,    type: string}
      - {name: flag,     type: boolean}
      - {name: pit_rate, type: double}
      - {name: score,    type: long}
      - {name: time,     type: timestamp, format: '%Y-%m-%d %H:%M:%S'}
      - {name: purchase, type: timestamp, format: '%Y/%m/%d'}
```

## Uage

```shell

% cat example/config.yml
  in:
    type: randomj
    rows: 16
    threads: 1
    # default_timezone: Asia/Tokyo
    primary_key: myid
    schema:
      - {name: myid,     type: long}
      - {name: named,    type: string}
      - {name: x_flag,   type: boolean}
      - {name: pit_rate, type: double}
      - {name: score,    type: long}
      - {name: time,     type: timestamp, format: '%Y-%m-%d %H:%M:%S'}
      - {name: purchase, type: timestamp, format: '%Y/%m/%d'}
  
  out:
    type: stdout


% embulk run -I lib example/config.yml
2017-07-09 15:46:41.895 +0900: Embulk v0.8.25
2017-07-09 15:46:55.699 +0900 [INFO] (0001:transaction): Loaded plugin embulk/input/randomj from a load path
2017-07-09 15:46:55.781 +0900 [INFO] (0001:transaction): Using local thread executor with max_threads=8 / output tasks 4 = input tasks 1 * 4
2017-07-09 15:46:55.801 +0900 [INFO] (0001:transaction): {done:  0 / 1, running: 0}
1,01mtx0RqBzw6wAWhIC1T8AnqppmQZdHl,true,9146.666957486106,5317,2017-08-14 00:59:43,2017/09/25
2,shGNGPfzZpyK9M2io2869LyMcw0pBOKq,true,4752.870731927854,8292,2017-09-12 02:31:04,2017/09/16
3,n5E8DCtCV2U3VzLhycdFgzNdLXcitXqx,false,5694.8421878399195,5604,2017-08-20 18:24:12,2017/09/29
4,soNWkF9sJryYje6Ah8WQ3E54rFhyptB7,true,2806.659132152377,7562,2017-09-13 03:38:13,2017/10/24
5,1a368i7mvl7c3Vfvg9S2dsrMivRK53Ag,false,3341.138399030519,1037,2017-10-12 04:38:38,2017/08/16
6,qwRsXYZcoVXA6DWPL40s7yybKQSM9tRQ,true,5250.281769589405,6277,2017-08-28 14:09:45,2017/08/15
7,AA6IaC9PU5d99l4hB0WjFvMMbHKa7j59,false,3431.3913221943226,6323,2017-09-07 13:40:42,2017/08/05
8,8ZCyzMgkx40yVqjVGBVsMkYuDoHRMmC7,true,2610.1924803136876,2548,2017-09-23 03:40:19,2017/07/14
9,S7T1Gyk7EupPEJAYAXCsqsR1KDy7uiyD,true,5285.107346872876,904,2017-08-31 20:47:02,2017/09/10
10,tGZJcz0uX7mnK2epBQGI1Uk2aGgJbK9w,false,9707.376365026039,5968,2017-08-31 21:27:20,2017/09/08
11,mEAnKKnyIaxrtUp0krjq18RMfMTlM2dB,true,7709.450184794057,4111,2017-08-29 16:35:22,2017/07/23
12,0ia5zVdwuPESDhI42ekDvvKNe1yxbwZG,true,6126.236771655601,3195,2017-10-08 19:21:30,2017/09/09
13,jOMm4H3S02vIc5bNAbwVxZs1TQjuBUeF,true,6000.891404245845,5257,2017-07-09 07:49:29,2017/08/22
14,o4T88GGY6qPzKSg9u2fdIGovYG3ssnj5,false,3640.521742574272,3756,2017-09-03 03:24:29,2017/10/05
15,mr7T7kTsTRo6SitrkJG5iVB2sf4jC8Pr,false,4838.126961869101,5924,2017-08-17 09:18:16,2017/10/18
16,oO0Eop9zzapIOunoSgrwV6eRgdVbwPiv,false,6363.525095467271,936,2017-10-12 04:07:29,2017/09/03
2017-07-09 15:46:55.994 +0900 [INFO] (0001:transaction): {done:  1 / 1, running: 0}
2017-07-09 15:46:56.030 +0900 [INFO] (main): Committed.
2017-07-09 15:46:56.031 +0900 [INFO] (main): Next config diff: {"in":{},"out":{}}

```


## Build

TBD
```
$ ./gradlew gem  # -t to watch change of files and rebuild continuously
```

```
$ ./gradlew build && ./gradlew classpath 
$ embulk run -I lib config/example.yml     
```
