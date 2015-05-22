package lt.mediapark.invitetravel

class CommonFilters {

    def usersService

    def filters = {

        printRequest(controller: '*', action: '*') {
            before = {
                if (request.JSON) {
                    log.info("JSON REQUEST: ${request.JSON}")
                } else {
                    log.info("NON-JSON REQUEST! Headers: ${request.getHeaderNames().collect {request.getHeader(it)}}")
                }
            }
        }

//        validatePicture(controller: 'pictures', action: '(index|delete)') {
//            before = {
//                def id = params.id
//                return !!Picture.exists(id)
//            }
//        }
//        allowDebugging(controller: 'debug', action: '*') {
//            before = {
//                return !!Environment.isDevelopmentMode()
//            }
//        }
//        controlRequestor(controller: '(users|pictures)', action: '(index|list|logout|update|delete|upload)') {
//            before =  {
//                def userId = params.requestor
//                boolean userThere = usersService.userReady(userId)
//                userThere || redirect(uri: '500')
//            }
//        }
    }
}
