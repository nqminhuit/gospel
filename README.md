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
Build:
```bash
podman build -t gospel . || return 1
podman create --name gospel_ gospel:latest
podman cp gospel_:/app/gospel/gospel .
podman rm -f gospel_
```

Run:
```bash
./gospel
```

Result:
```
2024/09/22
Con Người sẽ bị nộp. Ai muốn làm người đứng đầu, thì phải phục vụ mọi người.
✠Tin Mừng Chúa Giê-su Ki-tô theo thánh Mác-cô. Mc 9,30-37
Khi ấy, Đức Giê-su và các môn đệ đi băng qua miền Ga-li-lê. Nhưng Đức Giê-su
không muốn cho ai biết, vì Người đang dạy các môn đệ rằng : “Con Người sẽ bị nộp
vào tay người đời, họ sẽ giết chết Người, và ba ngày sau khi bị giết chết, Người
sẽ sống lại.” Nhưng các ông không hiểu lời đó, và các ông sợ không dám hỏi lại
Người.
Sau đó, Đức Giê-su và các môn đệ đến thành Ca-phác-na-um. Khi về tới nhà, Đức
Giê-su hỏi các ông : “Dọc đường, anh em đã bàn tán điều gì vậy ?” Các ông làm
thinh, vì khi đi đường, các ông đã cãi nhau xem ai là người lớn hơn cả. Rồi Đức
Giê-su ngồi xuống, gọi Nhóm Mười Hai lại mà nói : “Ai muốn làm người đứng đầu,
thì phải làm người rốt hết, và làm người phục vụ mọi người.” Kế đó, Người đem
một em nhỏ đặt vào giữa các ông, rồi ôm lấy nó và nói : “Ai tiếp đón một em nhỏ
như em này vì danh Thầy, là tiếp đón chính Thầy ; và ai tiếp đón Thầy, thì không
phải là tiếp đón Thầy, nhưng là tiếp đón Đấng đã sai Thầy.”
```

## Build from remote repository

Podman has the ability to [build from remote repository](https://blog.podman.io/2023/09/podman-fun-fact-of-the-day-git-builds/), If you only need the binary file without checking out the whole repository:
```bash
podman build -t gospel https://github.com/nqminhuit/gospel.git || return 1
podman create --name gospel_ gospel:latest
podman cp gospel_:/app/gospel/gospel .
podman rm -f gospel_
```
