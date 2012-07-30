/*
 * JBILLING CONFIDENTIAL
 * _____________________
 *
 * [2003] - [2012] Enterprise jBilling Software Ltd.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Enterprise jBilling Software.
 * The intellectual and technical concepts contained
 * herein are proprietary to Enterprise jBilling Software
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden.
 */

package ibilling

import grails.plugins.springsecurity.Secured
import com.infosense.ibilling.server.user.contact.db.ContactTypeDTO
import com.infosense.ibilling.server.user.contact.db.ContactDTO
import com.infosense.ibilling.server.user.db.CompanyDTO
import com.infosense.ibilling.server.util.db.LanguageDTO
import com.infosense.ibilling.server.util.db.InternationalDescription
import com.infosense.ibilling.server.ws.ContactTypeWS;
import com.infosense.ibilling.server.ws.InternationalDescriptionWS;
import com.infosense.ibilling.common.SessionInternalError

/**
 * ContactTypeConfigController 
 *
 * @author Brian Cowdery
 * @since 27-Jan-2011
 */
@Secured(["MENU_99"])
class ContactTypeConfigController {

    static pagination = [ max: 10, offset: 0 ]

    def webServicesSession
    def viewUtils
    def recentItemService
    def breadcrumbService

    def index = {
        redirect action: list, params: params
    }

    def list = {
        params.max = params?.max?.toInteger() ?: pagination.max
        params.offset = params?.offset?.toInteger() ?: pagination.offset

        def types = ContactTypeDTO.createCriteria().list(
                max:    params.max,
                offset: params.offset
        ) {
            eq('entity', new CompanyDTO(session['company_id']))
            order('id', 'asc')
        }

        def selected = params.id ? ContactTypeDTO.get(params.int("id")) : null

        breadcrumbService.addBreadcrumb(controllerName, actionName, null, params.int('id'))

        [ types: types, selected: selected, languages: languages ]
    }

    /**
     * Shows details of the selected contact type.
     */
    def show = {
        def selected = ContactTypeDTO.get(params.int('id'))
        breadcrumbService.addBreadcrumb(controllerName, 'list', null, params.int('id'))

        render template: 'show', model: [ selected: selected, languages: languages ]
    }

    /**
     * Show the "_edit.gsp" panel to create a new contact.
     */
    def edit = {
        render template: 'edit', model: [ languages: languages ]
    }

    /**
     * Saves a new contact type.
     */
    def save = {
        def contactType = new ContactTypeWS()
        contactType.companyId = session['company_id']
        contactType.primary = params.int('isPrimary')

        params.language.each { id, value ->
            contactType.descriptions.add(new InternationalDescriptionWS(id as Integer, value))
        }

        try {
            log.debug("creating new contact type ${contactType}")
            contactType.id = webServicesSession.createContactTypeWS(contactType)
			
			flash.message = 'contact.type.created'
			flash.args = [  contactType.id as String ]

        } catch (SessionInternalError e) {
            viewUtils.resolveException(flash, session.locale, e)
        }

        redirect action: list, id: contactType.id
    }

    def getLanguages() {
        return LanguageDTO.list()
    }
}
