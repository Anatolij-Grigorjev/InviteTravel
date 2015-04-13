import grails.rest.RestfulController

class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {

            }
        }

        "/debug/login" {
            controller = 'debug'
            action = 'login'
        }

        "/$controller/$id?"{
            constraints {
                id matches: /\d+/
            }
            action = 'index'
        }

        "/chat/$id1/$id2/$time?"{
            constraints {
                id1 matches: /\d+/
                id2 matches: /\d+/
                time matches: /\d+/
            }
            action = 'index'
            controller = 'chat'
        }
        "/chat/send/$id1/$id2"{
            constraints {
                id1 matches: /\d+/
                id2 matches: /\d+/
            }
            action = 'send'
            controller = 'chat'
        }



        "/" (view: 'index')
        "500"(view:'/error')
	}
}
