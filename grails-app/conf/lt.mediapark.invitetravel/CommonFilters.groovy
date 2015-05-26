package lt.mediapark.invitetravel

class CommonFilters {

    def usersService

    def filters = {

        printRequest(controller: '*', action: '*') {
            before = {
                log.info("DESTINATION: ${request.requestURL.append(request.queryString?:'')}")
                if (request.JSON) {
                    log.info("JSON REQUEST: ${request.JSON}")
                } else {
                    log.info("NON-JSON REQUEST! Headers: ${request.getHeaderNames().collect {request.getHeader(it)}}")
                }
            }
        }
        boostActivity(controller: 'pictures|debug', action: 'index', invert: true) {
            before = {
                if (actionName == 'login') return true
                if (params.requestor) {
                    log.info "Appearant activity from requestor ${params.requestor}"
                    Long id = Long.parseLong(params.requestor)
                    def user = usersService.get(id)
                    if (user) {
                        user.lastActive = new Date()
                        user.save(flush: true)
                        params['currUser'] = user
                    } else {
                        response.status = 403
                        return false
                    }
                } else {
                    log.info('Requestor not specified, denying request!')
                    response.status = 403
                    return false
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
