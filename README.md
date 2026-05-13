# frontend-app
앱 (프론트?)

# 환경 세팅 및 실행 방법

# 0. 깃허브- 안드로이드 스튜디오 연동 세팅 (최초 1회 필수)
안드로이드 스튜디오에 본인의 깃허브 계정이 연결되어 있지 않다면, 아래 과정을 먼저 진행해 주세요.

1. 안드로이드 스튜디오 상단 메뉴에서 **File > Settings** (Mac은 `Android Studio > Settings...`) 클릭
2. 좌측 메뉴에서 **Version Control > GitHub** 선택
3. 중앙의 **[+]** 버튼을 누르고 **Log In via GitHub...** 클릭
4. 인터넷 창이 열리면 로그인 후 **Authorize in GitHub** 버튼을 눌러 권한 승인
5. 안드로이드 스튜디오 화면에 본인의 깃허브 프로필이 뜨면 연동 완료! (OK 누르고 닫기)
6. 혹시 데탑이나 노트북에에 git을 설치안했으면 git 설치후 진행


# 1. 프로젝트 가져오기 (Clone) // 얘도 최초 1회 
1. 안드로이드 스튜디오를 켭니다.
2. 웰컴 화면에서 **Get from VCS**를 클릭합니다. 
(이미 다른 프로젝트가 열려있다면 상단 메뉴에서 `File > New > Project from Version Control` 선택)
3. Repository URL에 아래 주소를 입력하고 **Clone**을 누릅니다.
   https://github.com/2026-1-mobile-programming-team-3/frontend-app.git

   
### 2. main 브랜치를 기준으로 작업 // 작업할때마다
1. 터미널(alt+f12)에  git pull origin main (또는 자기브랜치)을 입력하고 작업 시작
2. 따로 브랜치를 만들어두고 작업하는걸 권장 (근데 최종 파트 완성은 main branch에 push를 해야됨)
3. pull하고 가져오면 처음 몇초동안 우리가 자주했던 Android 프로젝트 폴더가 안뜰텐데 시간지나면 뜸
4. 작업 완료후 프로젝트 파일 아래의 커밋 누르고 커밋 메시지 입력 후로 커밋
5. git push origin main(또는 자기브랜치) 터미널로 입력 후 작업 끝내기

+ 안드로이드 스튜디오에서 ctrl+shift+k 누르거나 commit and push누르면 push가 분명 되야되는데
나만 그런건지는 몰라도 push 안되서 안되시는분들은 터미널에 입력하십쇼...