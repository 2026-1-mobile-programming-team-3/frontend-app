# frontend-app
앱 프론트

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

   
# 2. main 브랜치를 기준으로 작업 // 작업할때마다
1. 터미널(alt+f12)에  git pull origin main (또는 자기브랜치)을 입력하고 작업 시작
2. 따로 브랜치를 만들어두고 작업하는걸 권장 (근데 최종 파트 완성은 main branch에 push를 해야됨)
3. pull하고 가져오면 처음 몇초동안 우리가 자주했던 Android 프로젝트 폴더가 안뜰텐데 시간지나면 뜸
4. 작업 완료후 프로젝트 파일 아래의 커밋 누르고 커밋 메시지 입력 후로 커밋
5. git push origin main(또는 자기브랜치) 터미널로 입력 후 작업 끝내기

+ 안드로이드 스튜디오에서 ctrl+shift+k 누르거나 commit and push누르면 push가 분명 되야되는데
나만 그런건지는 몰라도 push 안되서 안되시는분들은 터미널에 입력하십쇼...

# 3. 디자인 관련
1. 디자인을 최대한 figma에 맞게 고쳤는데 100퍼센트 완벽하게 반영은 못했습니다
2. 처음 작업할때 작업하는 파트가 figma 디자인이랑 차이점을 확인해주시고 좀 다르거나 불편한게 있으면 고쳐주십쇼
3. 고치는 방법은 여러가지가 있겠지만 ai 써서 figma 파일을 가져와서 고쳐야 될 경우에는 아래 방법으로 사용하는걸 권장드립니다

+ ai를 써서 figma로 연동시키기
1. 작업 프로젝트 화면이 아닌 www.figma.com 사이트에 들어가기
2. 자기프로필 -> 설정 -> 보안 -> 개인 엑세스 토큰 -> 새로운 토큰 생성으로 id 토큰 얻기
3. 파일을 코덱스나 클로드가 읽을려면 file_content:read에 체크하시고 토큰 생성하기
4. 토큰이랑 https://www.figma.com/design/BWkXUCXVxj7MMq1K66DG0y/%EC%8B%9C%ED%9D%A5%EA%B0%80%EA%B0%9C?node-id=0-1&p=f&m=dev
를 입력해서 적절히 ai한테 명령내려서 고치시면됩니다.
5. 문제는 이렇게 할경우 github push가 보안상의 이유로 거절되기 때문에(클로드 기준) commit/push 자체를 ai한테 맡기거나
토큰이 저장된 파일을 .gitignore 파일에 넣거나 문제가 되는 파일의 토큰 자체를 지우시고 커밋을 해야함.
