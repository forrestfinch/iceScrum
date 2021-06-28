/*
 * Copyright (c) 2014 Kagilum.
 *
 * This file is part of iceScrum.
 *
 * iceScrum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * iceScrum is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with iceScrum.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Authors:
 *
 * Vincent Barrier (vbarrier@kagilum.com)
 */


import com.mysql.jdbc.CommunicationsException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.model.NotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.transaction.CannotCreateTransactionException

class UrlMappings {

    static mappings = {

        name default: "/$controller/$action/$id?" {}
        // Scrum OS
        "/" {
            controller = 'scrumOS'
            action = 'index'
        }
        "/$action" {
            controller = 'scrumOS'
        }
        "/api" {
            controller = 'scrumOS'
            action = 'swagger'
        }
        name openAPI: "/openapi.json" {
            controller = 'scrumOS'
            action = 'openAPI'
        }
        "/robots.txt" {
            controller = 'scrumOS'
            action = 'robots'
        }
        "/browserconfig.xml" {
            controller = 'scrumOS'
            action = 'browserconfig'
        }
        "/textileParser" {
            controller = 'scrumOS'
            action = 'textileParser'
        }
        // Permalinks
        "/$project-F$uid/$tab?" {
            controller = 'feature'
            action = 'permalink'
            constraints {
                project(matches: /[0-9A-Z]*/)
                uid(matches: /[0-9]*/)
                tab(inList: ['activities', 'comments', 'stories'])
            }
        }
        "/$project-T$uid/$tab?" {
            controller = 'task'
            action = 'permalink'
            constraints {
                project(matches: /[0-9A-Z]*/)
                uid(matches: /[0-9]*/)
                tab(inList: ['activities', 'comments'])
            }
        }
        "/$project-$uid/$tab?" {
            controller = 'story'
            action = 'permalink'
            constraints {
                project(matches: /[0-9A-Z]*/)
                uid(matches: /[0-9]*/)
                tab(inList: ['tasks', 'tests', 'comments', 'activities'])
            }
        }
        // Legacy permalinks
        "/p/$project-F$uid/" {
            controller = 'feature'
            action = 'permalink'
            constraints {
                project(matches: /[0-9A-Z]*/)
                uid(matches: /[0-9]*/)
            }
        }
        "/p/$project-T$uid/" {
            controller = 'task'
            action = 'permalink'
            constraints {
                project(matches: /[0-9A-Z]*/)
                uid(matches: /[0-9]*/)
            }
        }
        "/p/$project-$uid/" {
            controller = 'story'
            action = 'permalink'
            constraints {
                project(matches: /[0-9A-Z]*/)
                uid(matches: /[0-9]*/)
            }
        }
        // Widget in null workspace
        "/ui/widget" {
            controller = 'widget'
            action = [GET: "index", POST: "save"]
        }
        "/ui/widget/$widgetDefinitionId/$id?" {
            controller = 'widget'
            action = [GET: "show", POST: "update", DELETE: "delete"]
            constraints {
                widgetDefinitionId(matches: /[a-zA-Z]*/)
                id(matches: /\d*/)
            }
        }
        "/ui/widget/definitions" {
            controller = 'widget'
            action = 'definitions'
        }
        // Window in null workspace
        "/ui/window/$windowDefinitionId" {
            controller = 'window'
            action = [GET: "show", POST: "update"]
            constraints {
                windowDefinitionId(matches: /[a-zA-Z]*/)
            }
        }
        // Window settings in null workspace
        "/ui/window/$windowDefinitionId/settings" {
            controller = 'window'
            action = [GET: "settings", POST: "updateSettings"]
            constraints {
                windowDefinitionId(matches: /[a-zA-Z]*/)
            }
        }
        // Progress
        "/progress" {
            controller = 'scrumOS'
            action = 'progress'
        }
        // User
        "/user" {
            controller = 'user'
            action = [GET: "index", POST: "save"]
        }
        "/user/register/$token?" {
            controller = 'user'
            action = [GET: "register", POST: "save"]
            constraints {
                token(matches: /[0-9a-z]*/)
            }
        }
        "/user/retrieve" {
            controller = 'user'
            action = [GET: "retrieve", POST: "retrieve"]
        }
        "/user/initialsAvatar" {
            controller = 'user'
            action = 'initialsAvatar'
        }
        "/user/$id" {
            controller = 'user'
            action = [GET: "show", PUT: "update", POST: "update", DELETE: "delete"]
            constraints {
                id(matches: /\d*/)
            }
        }
        "/user/$id/activities" {
            controller = 'user'
            action = 'activities'
            constraints {
                id(matches: /\d*/)
            }
        }
        "/user/$id/menu" {
            controller = 'user'
            action = [POST: "menu"]
        }
        "/user/$id/unreadActivitiesCount" {
            controller = 'user'
            action = 'unreadActivitiesCount'
            constraints {
                id(matches: /\d*/)
            }
        }
        "/user/$id/avatar" {
            controller = 'user'
            action = 'avatar'
            constraints {
                id(matches: /\d*/)
            }
        }
        "/user/current" {
            controller = 'user'
            action = [GET: "current"]
        }
        "/user/roles/project/$project" {
            controller = 'user'
            action = [GET: 'roles']
            constraints {
                project(matches: /[0-9A-Z]*/)
            }
        }
        "/user/available/$property" {
            controller = 'user'
            action = [POST: "available"]
            constraints {
                property(inList: ['username', 'email'])
            }
        }
        //User Token
        "/user/$userId/token" {
            controller = 'userToken'
            action = [GET: 'index', POST: 'save']
            constraints {
                userId(matches: /\d*/)
            }
        }
        "/user/$userId/token/$id" {
            controller = 'userToken'
            action = [DELETE: 'delete']
            constraints {
                userId(matches: /\d*/)
                id(matches: /[0-9a-z]*/)
            }
        }
        // Feed
        "/feed/$project" {
            controller = 'project'
            action = 'feed'
            constraints {
                project(matches: /[0-9A-Z]*/)
            }
        }
        // Project
        "/project" {
            controller = 'project'
            action = [POST: "save"]
        }
        "/project/$action" {
            controller = 'project'
            constraints {
                action(inList: ['import', 'importDialog'])
            }
        }
        "/project/user/$id?" {
            controller = 'project'
            action = 'listByUser'
            constraints {
                id(matches: /\d*/)
            }
        }
        "/project/user/$id/$role" {
            controller = 'project'
            action = 'listByUserAndRole'
            constraints {
                id(matches: /\d*/)
                role(inList: ['productOwner', 'stakeHolder', 'inProject'])
            }
        }
        "/project/portfolio/$portfolio" {
            controller = 'project'
            action = 'listByPortfolio'
            constraints {
                portfolio(matches: /\d*/)
            }
        }
        "/project/team/$team" {
            controller = 'project'
            action = 'listByTeam'
            constraints {
                team(matches: /\d*/)
            }
        }
        "/project/$project/leaveTeam" {
            controller = 'project'
            action = 'leaveTeam'
            constraints {
                project(matches: /\d*/)
            }
        }
        "/project/$project/activities" {
            controller = 'project'
            action = 'activities'
            constraints {
                project(matches: /\d*/)
            }
        }
        "/project/$project/updateTeam" {
            controller = 'project'
            action = 'updateTeam'
        }
        "/project/$project/archive" {
            controller = 'project'
            action = 'archive'
        }
        "/project/$project/unArchive" {
            controller = 'project'
            action = 'unArchive'
        }
        "/project/$project/$action" {
            controller = 'project'
            constraints {
                action(inList: ['flowCumulative', 'velocityCapacity', 'velocity', 'parkingLot', 'burndown', 'burnup'])
            }
        }
        "/project/$project" {
            controller = 'project'
            action = [GET: "show", DELETE: "delete", POST: "update"]
            constraints {
                project(matches: /\d*/) // pkey is not accepted, must be the ID
            }
        }
        // New project
        "/project/available/$property" {
            controller = 'project'
            action = [POST: "available"]
            constraints {
                property(inList: ['name', 'pkey'])
            }
        }
        // Update project
        "/project/$project/available/$property" {
            controller = 'project'
            action = [POST: "available"]
            constraints {
                project(matches: /\d*/)
                property(inList: ['name', 'pkey'])
            }
        }
        // Export
        "/p/$project/export/$format?" {
            controller = 'project'
            action = 'export'
            constraints {
                project(matches: /[0-9A-Z]*/)
                format(inList: ['zip', 'xml'])
            }
        }
        "/p/$project/exportDialog" {
            controller = 'project'
            action = 'exportDialog'
            constraints {
                project(matches: /[0-9A-Z]*/)
            }
        }
        // Team
        "/team" {
            controller = 'team'
            action = [GET: "index", POST: "save"]
        }
        "/team/$id" {
            controller = 'team'
            action = [GET: "show", POST: "update", DELETE: "delete"]
            constraints {
                id(matches: /\d*/)
            }
        }
        "/team/project/$project" {
            controller = 'team'
            action = 'showByProject'
            constraints {
                project(matches: /[0-9A-Z]*/)
            }
        }
        // Widget
        "/widget/feed" {
            controller = 'feed'
            action = [POST: "index"]
        }
        // Portfolio
        "/portfolio" {
            controller = 'portfolio'
            action = [POST: "save"]
        }
        "/portfolio/$portfolio" {
            controller = 'portfolio'
            action = [GET: "show", DELETE: "delete", POST: "update"]
            constraints {
                portfolio(matches: /\d*/) // fkey is not accepted, must be the ID
            }
        }
        "/portfolio/available/$property" {
            controller = 'portfolio'
            action = [POST: "available"]
            constraints {
                property(inList: ['name', 'fkey'])
            }
        }
        "/portfolio/$portfolio/available/$property" {
            controller = 'portfolio'
            action = [POST: "available"]
            constraints {
                portfolio(matches: /\d*/)
                property(inList: ['name', 'fkey'])
            }
        }
        "/hook" {
            controller = 'hook'
            action = [GET: "index", POST: "save"]
        }
        "/hook/$id" {
            controller = 'hook'
            action = [GET: "show", PUT: "update", DELETE: 'delete', POST: 'update']
            constraints {
                id(matches: /\d+(,\d+)*/)
            }
        }
        "/clientOauth/token/$providerId" {
            controller = 'clientOauth'
            action = [POST: 'token']
            constraints {
                providerId(matches: /[0-9A-Za-z]*/)
            }
        }
        "/clientOauth/redirectUri" {
            controller = 'clientOauth'
            action = [GET: 'redirectUri']
        }
        // Errors mapping
        "401"(controller: "errors", action: "error401")
        "403"(controller: "errors", action: "error403")
        "404"(controller: "errors", action: "error404")
        "500"(controller: "errors", action: "error403", exception: BadCredentialsException)
        "500"(controller: "errors", action: "error403", exception: AccessDeniedException)
        "500"(controller: "errors", action: "error403", exception: NotFoundException)
        "500"(controller: 'errors', action: 'memory', exception: OutOfMemoryError)
        "500"(controller: 'errors', action: 'database', exception: CannotCreateTransactionException)
        "500"(controller: 'errors', action: 'database', exception: CommunicationsException)
        "500"(controller: 'errors', action: 'error500')
    }
}
