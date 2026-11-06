# Gson은 필드 이름으로 JSON을 읽고 쓴다. 이름이 바뀌면 서버가 위치 메시지를 읽지 못한다.
# - WebSocket 위치 메시지와 서버 거절 응답(WebSocketDataSource 안의 data class)
-keepclassmembers class com.tukorea.driver.data.websocket.WebSocketDataSource$* { <fields>; <init>(...); }
# - 번호 확인 응답
-keep class com.tukorea.driver.data.api.AvailabilityResponse { <fields>; <init>(...); }

# 노선 경로(assets/shuttle_routes.json)를 TypeToken으로 읽는다. 제네릭 정보가 지워지면 읽지 못한다.
-keepattributes Signature
-keep class * extends com.google.gson.reflect.TypeToken
