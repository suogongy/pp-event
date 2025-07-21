
namespace java org.ppj.pp.event.sample.thrift


struct CommonResponse {
    1:i32 code
    2:string message
    3:string result
}

service PPEventSampleService {

    CommonResponse findUserById(1:i64 userId)

    CommonResponse addUser(1:i64 number,2:string name,3:i32 age,4:i32 sex)
}
