/*
 * Copyright (c) 2016 Kagilum SAS.
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
 * Nicolas Noullet (nnoullet@kagilum.com)
 * Colin Bontemps (cbontemps@kagilum.com)
 */


import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.userdetails.GrailsUser
import org.codehaus.groovy.grails.plugins.web.taglib.JavascriptTagLib
import org.icescrum.core.domain.*
import org.icescrum.core.domain.security.Authority
import org.icescrum.core.security.rest.TokenExtractor
import org.icescrum.core.services.SecurityService
import org.icescrum.core.support.ApplicationSupport
import org.icescrum.web.JQueryProvider

import javax.naming.InitialContext

icescrum {

    /* Administration */
    registration.enable = true
    login.retrieve.enable = false
    invitation.enable = false
    user.search.enable = true
    gravatar.enable = true
    announcement.enable = true

    alerts {
        subject_prefix = "[icescrum]"
        enable = false
        emailPerAccount = false
        errors.to = "dev@icescrum.org"
    }

    alerts.default.from = "webmaster@icescrum.org"

    mail {
        envelopeFrom = null
    }

    sessionTimeoutSeconds = 60 * 30 // 30 minutes default
    try {
        String extConfFile = (String) new InitialContext().lookup("java:comp/env/icescrum.timezone.default")
        if (extConfFile) {
            timezone.default = extConfFile
        }
    } catch (Exception e) {
        timezone.default = System.getProperty('user.timezone') ?: 'UTC'
    }

    push.enable = true

    try {
        String extConfFile = (String) new InitialContext().lookup("java:comp/env/icescrum.basedir")
        if (extConfFile) {
            baseDir = extConfFile
        }
    } catch (Exception e) {
        baseDir = new File(System.getProperty('user.home'), appName).canonicalPath
    }

    auto_follow_productowner = true
    auto_follow_stakeholder = true
    auto_follow_scrummaster = true

    activities.important = [Activity.CODE_SAVE, 'updateState']

    cors {
        enable = true
        urlPatterns = ['/ws/*', '/assets/*']
        allowedHeaders = [TokenExtractor.TOKEN_HEADER]
    }

    check {
        enable = true
        url = 'https://www.icescrum.com'
        path = 'wp-json/kagilum/v1/version'
        interval = 1440 // in minutes (24h)
        timeout = 5000
    }

    reportUsage {
        enable = true
        url = 'https://www.icescrum.com'
        path = 'wp-json/kagilum/v1/report'
        interval = 1440
        timeout = 5000
    }

    feedback.enable = true

    /* Server warnings to display to users */
    warnings = []

    /* Project administration */
    project {
        export.enable = true
        creation.enable = true
    }
    project.import.enable = true
    project.private.enable = true
    project.private.default = false

    /* Portfolio administration */
    portfolio {
        export.enable = false
        creation.enable = true
    }
    portfolio.import.enable = false

    workspaces = [
            project: [
                    path        : 'p',
                    type        : WorkspaceType.PROJECT,
                    objectClass : Project,
                    config      : { project -> [key: project.pkey, path: 'p'] },
                    params      : { project -> [project: project.id] },
                    indexScrumOS: { projectWorkspace, User user, SecurityService securityService, SpringSecurityService springSecurityService ->
                        def project = projectWorkspace.object
                        if (project?.preferences?.hidden && !securityService.inProject(project, springSecurityService.authentication) && !securityService.stakeHolder(project, springSecurityService.authentication, false)) {
                            return false
                        }
                        if (project && user && !user.admin && user.preferences.lastProjectOpened != project.pkey) {
                            user.preferences.lastProjectOpened = project.pkey
                            user.save()
                        }
                        return true
                    },
                    enabled     : { application -> true },
                    hooks       : [
                            events       : [
                                    "feature.create", "feature.update", "feature.delete", "feature.addedComment", "feature.updatedComment", "feature.removedComment",
                                    "story.create", "story.update", "story.delete", "story.state", "story.addedComment", "story.updatedComment", "story.removedComment",
                                    "task.create", "task.update", "task.delete", "task.state", "task.addedComment", "task.updatedComment", "task.removedComment",
                                    "release.create", "release.update", "release.delete", "release.state",
                                    "sprint.create", "sprint.update", "sprint.delete", "sprint.state",
                                    "acceptanceTest.create", "acceptanceTest.update", "acceptanceTest.delete", "acceptanceTest.state",
                                    "actor.create", "actor.update", "actor.delete",
                                    "meeting.create", "meeting.update", "meeting.delete"
                            ],
                            defaultEvents: ["story.create", "story.update", "story.delete", "story.state"],
                    ]
            ]
    ]

    hooks {
        events = ["user.create", "user.update", "user.delete", "project.create", "project.update", "project.delete"]
        enable = false
        disableAfterErrors = 5
        httpTimeout = new Integer(3 * 1000)
        socketTimeout = new Integer(10 * 1000)
    }

    atmosphere {
        maxUsers = []
        liveUsers = []
        maxConnections = 0
        liveConnections = 0
    }

    securitydebug.enable = false
    pushdebug.enable = false
    profiling.enable = false
    log.dir = null // Fix ilog dir due to lazy object initialization - init object

    marshaller = [
            portfolio           : [include: ['businessOwners', 'stakeHolders', 'invitedBusinessOwners', 'invitedStakeHolders'],
                                   asShort: ['name', 'fkey'],
                                   textile: ['description']],
            story               : [include: ['testState', 'tags', 'dependences', 'countDoneTasks', 'totalRemainingTime', 'project', 'permalink'],
                                   exclude: ['voters', 'metaDatas'],
                                   withIds: ['actors', 'followers'],
                                   textile: ['notes'],
                                   asShort: ['state', 'effort', 'uid', 'name', 'rank', 'project', 'permalink']],
            project             : [include: ['owner', 'productOwners', 'stakeHolders', 'invitedStakeHolders', 'invitedProductOwners', 'simpleProjectApps', 'team', 'storyStateNames'],
                                   asShort: ['pkey'],
                                   exclude: ['cliches', 'teams', 'goal', 'metaDatas'],
                                   textile: ['description']],
            team                : [include: ['members', 'scrumMasters', 'invitedScrumMasters', 'invitedMembers', 'owner']],
            task                : [exclude: ['participants', 'spent', 'metaDatas'],
                                   textile: ['notes'],
                                   asShort: ['permalink'],
                                   include: ['tags', 'sprint', 'permalink']],
            user                : [exclude: ['password', 'accountExpired', 'accountLocked', 'passwordExpired', 'tokens', 'preferences'],
                                   asShort: ['firstName', 'lastName'],
                                   include: ['admin']],
            actor               : [asShort: ['name']],
            feature             : [include: ['countDoneStories', 'state', 'effort', 'tags', 'inProgressDate', 'project', 'actualReleases', 'permalink'],
                                   exclude: ['metaDatas'],
                                   withIds: ['stories'],
                                   textile: ['notes'],
                                   asShort: ['color', 'name', 'permalink']],
            sprint              : [include: ['activable', 'reactivable', 'totalRemaining', 'duration', 'index', 'plannedVelocity', 'fullName'],
                                   exclude: ['cliches', 'metaDatas'],
                                   withIds: ['stories'],
                                   textile: ['retrospective', 'doneDefinition'],
                                   asShort: ['state', 'capacity', 'velocity', 'orderNumber', 'parentReleaseId', 'hasNextSprint', 'reactivable', 'parentReleaseName', 'parentReleaseOrderNumber', 'deliveredVersion', 'index', 'plannedVelocity']],
            release             : [include: ['duration', 'closable', 'activable', 'reactivable'],
                                   textile: ['vision'],
                                   asShort: ['name', 'state', 'endDate', 'startDate', 'orderNumber'],
                                   exclude: ['cliches', 'metaDatas']
            ],
            backlog             : [include: ['count', 'isDefault'],
                                   textile: ['notes']],
            activity            : [include: ['important']],
            widget              : [include: ['width', 'height'],
                                   exclude: ['userPreferences']],
            usertoken           : [:],
            userpreferences     : [include: ['emailsSettings'],
                                   exclude: ['user', 'menu', 'emailsSettingsData', 'widgets']],
            projectpreferences  : [asShort: ['archived', 'noEstimation', 'autoDoneStory', 'autoDoneFeature', 'autoInReviewStory', 'displaySprintGoal', 'displayRecurrentTasks', 'displayUrgentTasks', 'hidden', 'limitUrgentTasks', 'assignOnCreateTask',
                                             'stakeHolderRestrictedViews', 'assignOnBeginTask', 'autoCreateTaskOnEmptyStory', 'timezone', 'estimatedSprintsDuration', 'hideWeekend']],
            attachment          : [include: ['filename']],
            acceptancetest      : [include: ['parentProject'],
                                   exclude: ['metaDatas'],
                                   textile: ['description'],
                                   asShort: ['state', 'parentProject']],
            template            : [asShort: ['name']],
            simpleprojectapp    : [include: ['availableForServer', 'enabledForServer'],
                                   exclude: ['parentProject']],
            timeboxnotestemplate: [include: ['configs'],
                                   exclude: ['configsData']],
            invitation          : [include: ['project', 'team', 'portfolio']],
            hook                : [:],
            meeting             : [:]
    ]

    resourceBundles = [
            featureTypes           : [
                    (Feature.TYPE_FUNCTIONAL): 'is.feature.type.functional',
                    (Feature.TYPE_ENABLER)   : 'is.feature.type.enabler'
            ],
            featureStates          : [
                    (Feature.STATE_DRAFT): 'is.feature.state.draft',
                    (Feature.STATE_WAIT) : 'is.feature.state.wait',
                    (Feature.STATE_BUSY) : 'is.feature.state.inprogress',
                    (Feature.STATE_DONE) : 'is.feature.state.done'
            ],
            storyStates            : [
                    (Story.STATE_FROZEN)    : 'is.story.state.frozen',
                    (Story.STATE_SUGGESTED) : 'is.story.state.suggested',
                    (Story.STATE_ACCEPTED)  : 'is.story.state.accepted',
                    (Story.STATE_ESTIMATED) : 'is.story.state.estimated',
                    (Story.STATE_PLANNED)   : 'is.story.state.planned',
                    (Story.STATE_INPROGRESS): 'is.story.state.inprogress',
                    (Story.STATE_INREVIEW)  : 'is.story.state.inReview',
                    (Story.STATE_DONE)      : 'is.story.state.done'
            ],
            storyStatesColor       : [
                    (Story.STATE_FROZEN)    : '#aaaaaa',
                    (Story.STATE_SUGGESTED) : '#ffcc01',
                    (Story.STATE_ACCEPTED)  : '#ff6b1c',
                    (Story.STATE_ESTIMATED) : '#ff3333',
                    (Story.STATE_PLANNED)   : '#c88cff',
                    (Story.STATE_INPROGRESS): '#00abfc',
                    (Story.STATE_INREVIEW)  : '#002ee8',
                    (Story.STATE_DONE)      : '#27d285'
            ],
            storyTypes             : [
                    (Story.TYPE_USER_STORY)     : 'is.story.type.story',
                    (Story.TYPE_DEFECT)         : 'is.story.type.defect',
                    (Story.TYPE_TECHNICAL_STORY): 'is.story.type.technical'
            ],
            storyTypesColor        : [
                    (Story.TYPE_USER_STORY)     : '#4cd1b0',
                    (Story.TYPE_DEFECT)         : '#d0021b',
                    (Story.TYPE_TECHNICAL_STORY): '#4a90e2'
            ],
            storyTypesCliche       : [
                    (Story.TYPE_USER_STORY)     : Cliche.FUNCTIONAL_STORY_PROJECT_REMAINING_POINTS,
                    (Story.TYPE_DEFECT)         : Cliche.DEFECT_STORY_PROJECT_REMAINING_POINTS,
                    (Story.TYPE_TECHNICAL_STORY): Cliche.TECHNICAL_STORY_PROJECT_REMAINING_POINTS
            ],
            releaseStates          : [
                    (Release.STATE_WAIT)      : 'is.release.state.wait',
                    (Release.STATE_INPROGRESS): 'is.release.state.inprogress',
                    (Release.STATE_DONE)      : 'is.release.state.done'
            ],
            sprintStates           : [
                    (Sprint.STATE_WAIT)      : 'is.sprint.state.wait',
                    (Sprint.STATE_INPROGRESS): 'is.sprint.state.inprogress',
                    (Sprint.STATE_DONE)      : 'is.sprint.state.done'
            ],
            taskStates             : [
                    (Task.STATE_WAIT): 'is.task.state.wait',
                    (Task.STATE_BUSY): 'is.task.state.inprogress',
                    (Task.STATE_DONE): 'is.task.state.done'
            ],
            taskTypes              : [
                    (Task.TYPE_RECURRENT): 'is.task.type.recurrent',
                    (Task.TYPE_URGENT)   : 'is.task.type.urgent'
            ],
            roles                  : [
                    (Authority.MEMBER)              : 'is.role.teamMember',
                    (Authority.SCRUMMASTER)         : 'is.role.scrumMaster',
                    (Authority.PRODUCTOWNER)        : 'is.role.productOwner',
                    (Authority.STAKEHOLDER)         : 'is.role.stakeHolder',
                    (Authority.PO_AND_SM)           : 'is.role.poAndSm',
                    (Authority.BUSINESSOWNER)       : 'is.role.businessOwner',
                    (Authority.PORTFOLIOSTAKEHOLDER): 'is.role.portfolioStakeHolder'
            ],
            planningPokerGameSuites: [
                    (PlanningPokerGame.FIBO_SUITE)   : 'is.estimationSuite.fibonacci',
                    (PlanningPokerGame.INTEGER_SUITE): 'is.estimationSuite.integer',
                    (PlanningPokerGame.CUSTOM_SUITE) : 'is.estimationSuite.custom',
            ],
            acceptanceTestStates   : [
                    (AcceptanceTest.AcceptanceTestState.TOCHECK.id): 'is.acceptanceTest.state.tocheck',
                    (AcceptanceTest.AcceptanceTestState.FAILED.id) : 'is.acceptanceTest.state.failed',
                    (AcceptanceTest.AcceptanceTestState.SUCCESS.id): 'is.acceptanceTest.state.success'
            ],
            backlogChartTypes      : [
                    'feature': 'is.feature',
                    'type'   : 'is.story.type',
                    'value'  : 'is.story.value',
                    'state'  : 'is.story.state',
                    'effort' : 'is.story.effort'
            ],
            backlogChartUnits      : [
                    'story' : 'todo.is.ui.stories',
                    'effort': 'is.story.effort'
            ]
    ]
    beta {
        enable = false
        features = ['usersOnline'] //should be [] if no features
    }
    security {
        authorizedTokenHeaders = ['X-Gitlab-Token', 'X-Auth-Token']
    }
    clientsOauth {} // Needs to be set for plugins
}

println "| Server Timezone: ${icescrum.timezone.default}"
println "| Java version: ${System.getProperty('java.version')}"

if (System.getProperty('https.proxyPort') || System.getProperty('http.proxyPort')) {
    println "| HTTP Client will use a Forward Proxy:"
    if (System.getProperty('https.proxyPort')) {
        println "| HTTPS Forward Proxy: ${System.getProperty('https.proxyHost')}:${System.getProperty('https.proxyPort')}"
    }
    if (System.getProperty('http.proxyPort')) {
        println "| HTTP Forward Proxy: ${System.getProperty('http.proxyHost')}:${System.getProperty('http.proxyPort')}"
    }
}

/* Headless mode */
System.setProperty("java.awt.headless", "true")

/*  Mail */
/*grails.mail.host = "smtp.gmail.com"
grails.mail.port = 465
grails.mail.username = "username@gmail.com"
grails.mail.password = ""
grails.mail.props = ["mail.smtp.auth":"true",
                     "mail.smtp.socketFactory.port":"465",
                     "mail.smtp.socketFactory.class":"javax.net.ssl.SSLSocketFactory",
                     "mail.smtp.socketFactory.fallback":"false"]*/

/* Assets */
grails.assets.excludes = ["**/*.scss"]
grails.assets.plugin."commentable".excludes = ["**/*"]
grails.assets.plugin."hd-image-utils".excludes = ["**/*"]
grails.assets.enableGzip = true

/*
 Attachmentable section
 */
grails.attachmentable.storyDir = { Story story -> ApplicationSupport.getAttachmentPath(WorkspaceType.PROJECT, story.backlog.id, 'stories', story.id) }
grails.attachmentable.featureDir = { Feature feature ->
    if (feature.backlog) {
        ApplicationSupport.getAttachmentPath(WorkspaceType.PROJECT, feature.backlog.id, 'features', feature.id)
    } else {
        ApplicationSupport.getAttachmentPath(WorkspaceType.PORTFOLIO, feature.portfolio.id, 'features', feature.id)
    }
}
grails.attachmentable.releaseDir = { Release release -> ApplicationSupport.getAttachmentPath(WorkspaceType.PROJECT, release.parentProject.id, 'releases', release.id) }
grails.attachmentable.sprintDir = { Sprint sprint -> ApplicationSupport.getAttachmentPath(WorkspaceType.PROJECT, sprint.parentRelease.parentProject.id, 'sprints', sprint.id) }
grails.attachmentable.projectDir = { Project project -> ApplicationSupport.getAttachmentPath(WorkspaceType.PROJECT, project.id, 'project', project.id) }
grails.attachmentable.taskDir = { Task task -> ApplicationSupport.getAttachmentPath(WorkspaceType.PROJECT, task.parentProject.id, 'tasks', task.id) }

grails.taggable.preserve.case = true

grails.plugin.databasemigration.updateOnStart = true
grails.plugin.databasemigration.updateOnStartFileNames = ['changelog.groovy']

/* Default grails config */
grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident'] // experiment
grails.mime.types = [
        html         : ['text/html', 'application/xhtml+xml'],
        xml          : ['text/xml', 'application/xml'],
        text         : 'text/plain',
        js           : 'text/javascript',
        rss          : 'application/rss+xml',
        atom         : 'application/atom+xml',
        css          : 'text/css',
        csv          : 'text/csv',
        all          : '*/*',
        json         : ['application/json', 'text/json'],
        form         : 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"

grails.controllers.defaultScope = 'singleton' // big experiment

grails.gorm.failOnError = true

// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'
// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// whether to install the java.util.logging bridge for sl4j. Disable fo AppEngine!
grails.logging.jul.usebridge = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
grails.mime.use.accept.header = true
grails.views.javascript.library = 'jquery'

// Parameters are only logged in dev, but we are never too safe
grails.exceptionresolver.params.exclude = ['password', 'user.password', 'user.confirmPassword', 'j_password', 'dataSource.password', 'grails.mail.password', 'grails.plugin.springsecurity.ldap.context.managerPassword', 'icescrum.migration.adminPassword']

environments {
    development {
        icescrum.serverURL = "http://localhost:8080/icescrum"
        icescrum.debug.enable = true
        grails.entryPoints.debug = false
        grails.tomcat.nio = true
    }
    test {
        icescrum.debug.enable = true
        grails.entryPoints.debug = false
        grails.tomcat.nio = true
    }
    production {
        icescrum.debug.enable = false
        grails.entryPoints.debug = false
    }
}

println "\n| Tmp directory: ${System.getProperty("java.io.tmpdir")}"

grails.cache.config = {
    cache {
        name 'feed'
        timeToLiveSeconds 120
    }
}

/*log4j = {
    def config = Holders.config
    def logLayoutPattern = new PatternLayout("%d [%t] %-5p %c %x - %m%n")

//    trace 'org.hibernate.type.descriptor.sql' // Uncomment to trace SQL variables bindings
//    debug 'org.hibernate.SQL'                 // Uncomment to trace SQL queries (or use logSql/formatSql in DataSource.groovy)

    error 'org.codehaus.groovy.grails.plugins',
            'org.grails.plugin',
            'grails.app'

    error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'

    warn 'org.mortbay.log'
    warn 'grails.plugin.cache'

    if (config.grails.entryPoints.debug) {
        debug 'org.icescrum.plugins.entryPoints'
    }

    if (ApplicationSupport.booleanValue(config.icescrum.debug.enable)) {
        debug 'grails.app.controllers.org.icescrum'
        debug 'grails.app.controllers.com.kagilum'
        debug 'grails.app.services.org.icescrum'
        debug 'grails.app.services.com.kagilum'
        debug 'grails.app.domain.org.icescrum'
        debug 'grails.app.domain.com.kagilum'
        debug 'org.icescrum.plugins.chat'
        debug 'grails.app.org.icescrum'
        debug 'org.icescrum.plugins'
//        debug 'net.sf.jasperreports'
        debug 'org.icescrum.core'
        debug 'com.kagilum'
    }

    if (ApplicationSupport.booleanValue(config.icescrum.pushdebug.enable)) {
        debug 'org.icescrum.atmosphere'
        debug "org.grails.plugins.atmosphere_meteor"
        debug 'org.atmosphere.cpr'
        debug 'org.atmosphere'
    } else {
        warn 'org.icescrum.atmosphere'
        warn "org.grails.plugins.atmosphere_meteor"
        warn 'org.atmosphere.cpr'
        warn 'org.atmosphere'
    }

    if (ApplicationSupport.booleanValue(config.icescrum.securitydebug.enable)) {
        debug 'org.springframework.security.saml'
        debug 'org.springframework.security'
        debug 'org.icescrum.core.security'
        debug 'com.kagilum.plugin.saml'
        debug 'com.kagilum.plugin.ldap'
        debug 'com.kagilum.plugin.preauth'
        //saml plugin
        debug 'org.springframework.security.saml'
        debug 'org.opensaml'
        debug 'logger.PROTOCOL_MESSAGE'
    }

    // Useless warning because are registered twice since it's based on controllerClazz.getMethods() which return the same method twice (1 with & 1 without params)
    error 'grails.plugin.springsecurity.web.access.intercept.AnnotationFilterInvocationDefinition'

    appenders {
        try {
            String extConfFile = (String) new InitialContext().lookup("java:comp/env/icescrum.log.dir")
            if (extConfFile) {
                config.icescrum.log.dir = extConfFile
            }
        } catch (Exception e) {
            config.icescrum.log.dir = System.getProperty('icescrum.log.dir') ?: config.icescrum.log.dir ?: new File('logs').absolutePath
        }
        //fix log dir due to lazy object initialization - save object to get it in bootStrapService
        System.setProperty('save.icescrum.log.dir', config.icescrum.log.dir)
        println "\n| Log directory: ${config.icescrum.log.dir}"

        appender new DailyRollingFileAppender(name: "icescrumFileLog",
                fileName: "${config.icescrum.log.dir}/${Metadata.current.'app.name'}.log",
                datePattern: "'.'yyyy-MM-dd",
                layout: logLayoutPattern
        )

        rollingFile name: "stacktrace", maxFileSize: 1024, file: "${config.icescrum.log.dir}/stacktrace.log"
    }

    root {
        if (ApplicationSupport.booleanValue(config.icescrum.debug.enable)) {
            debug 'stdout', 'icescrumFileLog'
            error 'stdout', 'icescrumFileLog'
            info 'stdout', 'icescrumFileLog'
        } else {
            debug 'icescrumFileLog'
            error 'icescrumFileLog'
            info 'icescrumFileLog'
        }
        additivity = true
    }

    off 'org.codehaus.groovy.grails.web.converters.JSONParsingParameterCreationListener'
    off 'org.codehaus.groovy.grails.web.converters.XMLParsingParameterCreationListener'
}*/

/* Security */
grails {
    plugin {
        springsecurity {
            password.algorithm = 'SHA-256'
            password.hash.iterations = 1

            rejectIfNoRule = false
            fii.rejectPublicInvocations = true
            controllerAnnotations.staticRules = [
                    //app controllers rules
                    '/grails-errorhandler'     : ['permitAll'],
                    '/stream/app/**'           : ['permitAll'],
                    '/scrumOS/**'              : ['permitAll'],
                    '/user/**'                 : ['permitAll'],
                    '/errors/**'               : ['permitAll'],
                    '/assets/**'               : ['permitAll'],
                    '/**/js/**'                : ['permitAll'],
                    '/**/css/**'               : ['permitAll'],
                    '/**/images/**'            : ['permitAll'],
                    '/**/favicon.ico'          : ['permitAll'],
                    '/oauth/authorize.dispatch': ["isFullyAuthenticated() and (request.getMethod().equals('GET') or request.getMethod().equals('POST'))"],
                    '/oauth/token.dispatch'    : ["isFullyAuthenticated() and request.getMethod().equals('POST')"]
            ]

            userLookup.userDomainClassName = 'org.icescrum.core.domain.User'
            userLookup.authorityJoinClassName = 'org.icescrum.core.domain.security.UserAuthority'
            authority.className = 'org.icescrum.core.domain.security.Authority'
            successHandler.alwaysUseDefault = false
            successHandler.targetUrlParameter = 'redirectTo'
            logout.targetUrlParameter = 'redirectTo'
            useBasicAuth = true
            basic.realmName = "Basic authentication for iceScrum"
            filterChain.chainMap = [
                    '/ws/**'          : 'JOINED_FILTERS,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-securityContextHolderAwareRequestFilter,-anonymousAuthenticationFilter,-basicAuthenticationFilter,-basicExceptionTranslationFilter', // Only token auth & oauth
                    '/**/project/feed': 'JOINED_FILTERS,-exceptionTranslationFilter,-tokenAuthenticationFilter,-restExceptionTranslationFilter,-statelessSecurityContextPersistenceFilter,-oauth2ProviderFilter,-clientCredentialsTokenEndpointFilter,-oauth2BasicAuthenticationFilter,-oauth2ExceptionTranslationFilter', // Session & basic auth
                    '/oauth/token'    : 'JOINED_FILTERS,-oauth2ProviderFilter,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-rememberMeAuthenticationFilter,-exceptionTranslationFilter',
                    '/**'             : 'JOINED_FILTERS,-tokenAuthenticationFilter,-restExceptionTranslationFilter,-basicAuthenticationFilter,-basicExceptionTranslationFilter,-statelessSecurityContextPersistenceFilter,-oauth2ProviderFilter,-clientCredentialsTokenEndpointFilter,-oauth2BasicAuthenticationFilter,-oauth2ExceptionTranslationFilter'// Only form auth with session
            ]

            rememberMe {
                cookieName = 'iceScrum'
                key = 'VincNicoJuShazam$'
            }

            useRunAs = true
            runAs.key = 'VincNicoJuShazam!'
            acl.authority.changeAclDetails = 'ROLE_RUN_AS_PERMISSIONS_MANAGER'

            useSecurityEventListener = true
            onInteractiveAuthenticationSuccessEvent = { e, appCtx ->
                def principal = e.authentication.principal
                if (principal instanceof GrailsUser) {
                    User.withTransaction {
                        def user = User.lock(principal.id)
                        user.lastLogin = new Date()
                        user.save(flush: true)
                    }
                }
            }

            oauthProvider {
                exceptionTranslationFilterStartPosition = SecurityFilterPosition.EXCEPTION_TRANSLATION_FILTER.order + 2
                oauthProvider.clientLookup.className = 'org.icescrum.core.domain.security.Client'
                authorizationCodeLookup.className = 'org.icescrum.core.domain.security.AuthorizationCode'
                accessTokenLookup.className = 'org.icescrum.core.domain.security.AccessToken'
                refreshTokenLookup.className = 'org.icescrum.core.domain.security.RefreshToken'
                tokenServices {
                    accessTokenValiditySeconds = 60 * 60 * 12 // default 12 hours
                    refreshTokenValiditySeconds = 60 * 60 * 24 * 30 // default 30 days
                }
            }
        }
    }
}

/* User config */
environments {
    production {
        def systemConfig = System.getProperty(ApplicationSupport.CONFIG_ENV_NAME)
        def envConfig = System.getenv(ApplicationSupport.CONFIG_ENV_NAME)
        def homeConfig = "${userHome}${File.separator}.icescrum${File.separator}config.groovy"
        println "--------------------------------------------------------"
        if (systemConfig && new File(systemConfig).exists()) {  // 1. System variable passed to the JVM : -Dicescrum.config.file=.../config.groovy
            println "Use configuration file provided a JVM system variable: " + systemConfig
            grails.config.locations = ["file:" + systemConfig]
        } else if (envConfig && new File(envConfig).exists()) { // 2. Environment variable icescrum.config.file=.../config.groovy
            println("Use configuration file provided by an environment variable: " + envConfig)
            grails.config.locations = ["file:" + envConfig]
        } else if (new File(homeConfig).exists()) {             // 3. Default location home/.icescrum/config.groovy
            println "Use configuration file from the iceScrum home: " + homeConfig
            grails.config.locations = ["file:" + homeConfig]
        } else {
            println "No configuration file found"
            grails.config.locations = []
        }
        try {
            String extConfFile = (String) new InitialContext().lookup('java:comp/env/' + ApplicationSupport.CONFIG_ENV_NAME)
            if (extConfFile) {
                grails.config.locations << extConfFile
                println "Use configuration file provided by JNDI: ${extConfFile}"
            }
        } catch (Exception e) {}
        println "(*) grails.config.locations = ${grails.config.locations}"
        println "--------------------------------------------------------"
    }
    test {
        icescrum.beta.enable = true
        icescrum.profiling.enable = true
        grails.mail.overrideAddress = "testing@kagilum.com"
        def systemConfig = System.getProperty(ApplicationSupport.CONFIG_ENV_NAME)
        def envConfig = System.getenv(ApplicationSupport.CONFIG_ENV_NAME)
        def homeConfig = "${userHome}${File.separator}.icescrum${File.separator}config.groovy"
        println "--------------------------------------------------------"
        if (systemConfig && new File(systemConfig).exists()) {  // 1. System variable passed to the JVM : -Dicescrum.config.file=.../config.groovy
            println "Use configuration file provided a JVM system variable: " + systemConfig
            grails.config.locations = ["file:" + systemConfig]
        } else if (envConfig && new File(envConfig).exists()) { // 2. Environment variable icescrum.config.file=.../config.groovy
            println("Use configuration file provided by an environment variable: " + envConfig)
            grails.config.locations = ["file:" + envConfig]
        } else if (new File(homeConfig).exists()) {             // 3. Default location home/.icescrum/config.groovy
            println "Use configuration file from the iceScrum home: " + homeConfig
            grails.config.locations = ["file:" + homeConfig]
        } else {
            println "No configuration file found"
            grails.config.locations = []
        }
        try {
            String extConfFile = (String) new InitialContext().lookup('java:comp/env/' + ApplicationSupport.CONFIG_ENV_NAME)
            if (extConfFile) {
                grails.config.locations << extConfFile
                println "Use configuration file provided by JNDI: ${extConfFile}"
            }
        } catch (Exception e) {}
        println "(*) grails.config.locations = ${grails.config.locations}"
        println "--------------------------------------------------------"
    }
    development {
        icescrum.beta.enable = true
        icescrum.profiling.enable = true
        grails.mail.overrideAddress = "testing@kagilum.com"
        grails.plugins.hibernateMetrics.enabled = true
        grails.plugins.hibernateMetrics.logSqlToConsole = false
        grails.plugin.springsecurity.controllerAnnotations.staticRules['/hibernateMetrics/**'] = ['permitAll']
    }
}

JavascriptTagLib.LIBRARY_MAPPINGS.jquery = ["jquery/jquery-${jQueryVersion}.min"]
JavascriptTagLib.PROVIDER_MAPPINGS.jquery = JQueryProvider.class

def uniqueCacheManagerName = appName + "-EhCacheManager-" + System.currentTimeMillis()
grails {
    cache {
        order = 2000 // higher than default (1000) and plugins, usually 1500
        enabled = true
        clearAtStartup = true // reset caches when redeploying
        ehcache {
            cacheManagerName = uniqueCacheManagerName
        }
        config = {
            provider {
                name uniqueCacheManagerName // unique name when configuring caches
            }
            sizeOfPolicy {
                maxDepth 100
                maxDepthExceededBehavior 'abort'
            }
            defaultCache {
                maxElementsInMemory 10000
            }
        }
    }
    // To improve perf
    gorm.default.mapping = {
        dynamicUpdate true
    }
    gorm.default.constraints = {
        if (ApplicationSupport.isMySQLUTF8mb4()) {
            grails.taggable.utf8mb4 = true
            keyMaxSize(maxSize: 191)
        } else {
            keyMaxSize([:]) //default
        }
    }
}

beans {
    cacheManager {
        shared = true
    }
}
