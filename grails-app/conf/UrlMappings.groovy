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
        "/debug/list/$num?" {
            controller ='debug'
            action = 'list'
        }

        "/users/subscriptions/extend"{
            controller = 'subscription'
            action = 'extend'
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
        "/chat/send/$id"{
            constraints {
                id matches: /\d+/
            }
            action = 'send'
            controller = 'chat'
        }



        "/" (view: 'index')
        "500"(view:'/error')
        "403"(view: '/unauth')
	}
}
