# Randomj input plugin for Embulk

[![Gem Version](https://badge.fury.io/rb/embulk-input-randomj.svg)](https://badge.fury.io/rb/embulk-input-randomj)
[![Build Status](https://travis-ci.org/yuokada/embulk-input-randomj.svg?branch=master)](https://travis-ci.org/yuokada/embulk-input-randomj)

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

- Add `length`, `max_value`, `min_value` option (from 0.3.0)
- Add `null_rate` option (from 0.4.0)  
  This configuration is that inserted `null` into `price` filed with a probability `8` of 10000.
- Support json type (from 0.5.0)  
  Experimental Feature
- Support `start_date` & `end_date` key in **Timestamp** field.

    - Ex1. `{name: created_at, type: timestamp, format: '%Y-%m-%d %H:%M:%S', start_date: 20180331, end_date: 20180430}`
    - Ex2. `{name: created_at, type: timestamp, format: '%Y-%m-%d %H:%M:%S', start_date: 20180331}`

```yaml
in:
    type: randomj
    rows: 16
    threads: 1
    primary_key: myid
    schema:
      - {name: myid,     type: long}
      - {name: named,    type: string, length: 12}
      - {name: price,    type: long, max_value: 1080, min_value: 100, null_rate: 8}
      - {name: purchase, type: timestamp, format: '%Y/%m/%d'}
      - {name: json_key, type: json, schema: '[{"name": "baz", "type": "array", "items": {"type": "string", "size": 1}}]' }
```


## Usage

### Example1

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

### Example2

- `named_s` return string with length 8
- `score` return value between `100~255`
- `rate` return value between `-100~100`

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
      - {name: named_s,  type: string, length: 8}
      - {name: x_flag,   type: boolean}
      - {name: rate,     type: double, max_value: 100, min_value: -100}
      - {name: score,    type: long, max_value: 255, min_value: 100}
      - {name: time,     type: timestamp, format: '%Y-%m-%d %H:%M:%S'}
      - {name: purchase, type: timestamp, format: '%Y/%m/%d'}

  out:
    type: stdout


% embulk run -I lib example/config.yml
2017-09-10 04:45:04.894 +0900: Embulk v0.8.32
2017-09-10 04:45:10.212 +0900 [INFO] (0001:transaction): Loaded plugin embulk/input/randomj from a load path
2017-09-10 04:45:10.246 +0900 [INFO] (0001:transaction): Using local thread executor with max_threads=8 / output tasks 4 = input tasks 1 * 4
2017-09-10 04:45:10.263 +0900 [INFO] (0001:transaction): {done:  0 / 1, running: 0}
1,BOcbVJX5bWL5wRBJc532trxvwhQpmg3d,yHwXATfG,true,-79.62544211154894,129,2017-12-05 22:31:35,2017/12/26
2,N2gljQxd4yDBzJjK9iSRUdROtaZGUEl7,zSrEMjzC,false,-11.47506884041689,194,2017-09-17 15:56:18,2017/12/06
3,PJvKkf0wwpGqGMlc7OjUhjZNi0pTEZIU,q6TgdoaZ,false,85.17356188437738,137,2017-10-07 17:28:43,2017/10/22
4,DA6wWE4p3zIPDK0Mp81bWczewNSMY2sq,KeobJmS1,false,79.95787440150436,221,2017-09-28 19:35:17,2017/11/20
5,8DNF4TzhVDLCFey2x1eCHryf4GdvHlyW,D2jddtEN,true,19.801687906161735,182,2017-11-24 18:43:38,2017/12/29
6,veyIxBc9u0FMwsGksMfLhvBMuIF2D7XO,6Mtz4MN9,true,26.922649237294582,176,2017-09-23 07:43:40,2017/11/18
7,HHCTLuaxAJIRHHG7cB2u9Ake9p9OSIcy,UHHKp5xX,true,9.960707451320626,108,2017-09-14 08:11:49,2017/11/05
8,HcQhHMQ4sYiXTBpvNiTqDGskuTeVEC6r,d0VSR8K8,false,-62.405292711551624,118,2017-11-11 08:06:20,2017/10/20
9,si5BWUPEEvVHvveeqSxG6ypc7pSsKtC7,bW5p9boG,false,-76.91915279000274,192,2017-09-28 19:46:53,2017/11/04
10,xnfU0aJgigJG9rPan2rwoffhN9pzLQCy,R8MV0Jpa,true,-79.40738909989871,104,2017-11-19 02:50:07,2017/09/11
11,KiRzQqfE6wRw3WjMPAmedqtHyG3MttGU,SowzDTSb,true,77.22509797548325,163,2017-12-23 18:16:30,2017/12/27
12,pQLz3fMIkN6UANwSbzJ5vhBWzF2FI7uo,uPGyHyuW,true,71.19680005107371,180,2017-11-23 16:31:30,2017/11/14
13,aFOc2qCAu5oYbxTCGkMNcZob6Tl3wl3Y,apFu34Ps,false,82.8406608691031,226,2017-10-03 06:09:25,2017/10/06
14,Kz3JGL23k7f8SR17xQBw063ApuGdeWIP,r0c0KnUC,true,-26.484829732050134,113,2017-10-01 02:40:37,2017/11/26
15,p5vGY02BzrHqk345JyAhFU7xVsA2jEZD,nhzsefns,false,-79.0184308849151,119,2017-12-15 22:59:28,2017/11/25
16,1jyxot60lCrRFMUfjyHcZ07dq05eu76a,WewnLZfw,false,-55.315211168770816,141,2017-12-11 10:36:46,2017/12/05
2017-09-10 04:45:10.344 +0900 [INFO] (0001:transaction): {done:  1 / 1, running: 0}
2017-09-10 04:45:10.351 +0900 [INFO] (main): Committed.
2017-09-10 04:45:10.351 +0900 [INFO] (main): Next config diff: {"in":{},"out":{}}

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


## ChangeLog

### 0.5.1

- Support start_date & end_date key with Timestamp field.

### 0.5.0

- Support `json` datatype (Experimental Feature)

### v0.4

- Support null_rate parameter
