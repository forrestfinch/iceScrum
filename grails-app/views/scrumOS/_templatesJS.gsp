%{--
- Copyright (c) 2015 Kagilum SAS.
-
- This file is part of iceScrum.
-
- iceScrum is free software: you can redistribute it and/or modify
- it under the terms of the GNU Affero General Public License as published by
- the Free Software Foundation, either version 3 of the License.
-
- iceScrum is distributed in the hope that it will be useful,
- but WITHOUT ANY WARRANTY; without even the implied warranty of
- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
- GNU General Public License for more details.
-
- You should have received a copy of the GNU Affero General Public License
- along with iceScrum.  If not, see <http://www.gnu.org/licenses/>.
-
- Authors:
-
- Vincent Barrier (vbarrier@kagilum.com)
- Nicolas Noullet (nnoullet@kagilum.com)
--}%
<%@ page import="org.icescrum.core.utils.BundleUtils; grails.converters.JSON;" %>
<div class='templates'>
    <g:render template="templates"/>
    <g:render template="/team/templates"/>
    <g:render template="/sprint/templates"/>
    <g:render template="/project/templates"/>
    <g:render template="/task/templates/task.light"/>
    <g:if test="${params.product}">
        <g:render template="/story/templates"/>
        <g:render template="/task/templates"/>
        <g:render template="/comment/templates"/>
        <g:render template="/attachment/templates"/>
        <g:render template="/activity/templates"/>
        <g:render template="/feature/templates"/>
        <g:render template="/acceptanceTest/templates"/>
        <g:render template="/release/templates"/>
        <g:render template="/backlog/templates"/>
    </g:if>
</div>
<script type="text/javascript">
    isSettings = {
        user:${user as JSON},
        roles:${roles as JSON},
        project:${product as JSON},
        pushContext:${product?.id?:"''"},
        messages:${i18nMessages as JSON},
        bundles:${is.i18nBundle() as JSON},
        applicationMenus:${applicationMenus as JSON},
        types: {
            task:${BundleUtils.taskTypes.keySet() as JSON},
            story:${BundleUtils.storyTypes.keySet() as JSON},
            feature:${BundleUtils.featureTypes.keySet() as JSON},
            planningPoker:${BundleUtils.planningPokerGameSuites.keySet() as JSON}
        },
        states: {
            task:${BundleUtils.taskStates.keySet() as JSON},
            acceptanceTest:${BundleUtils.acceptanceTestStates.keySet() as JSON}
        },
        serverUrl: "${grailsApplication.config.grails.serverURL}"
    };
</script>