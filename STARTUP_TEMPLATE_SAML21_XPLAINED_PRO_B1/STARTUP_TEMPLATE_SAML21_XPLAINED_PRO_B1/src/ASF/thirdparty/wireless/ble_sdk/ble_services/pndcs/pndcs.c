/**
 * \file
 *
 * \brief Battery service
 *
 * Copyright (c) 2016 Atmel Corporation. All rights reserved.
 *
 * \asf_license_start
 *
 * \page License
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name of Atmel may not be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * 4. This software may only be redistributed and used in connection with an
 *    Atmel microcontroller product.
 *
 * THIS SOFTWARE IS PROVIDED BY ATMEL "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT ARE
 * EXPRESSLY AND SPECIFICALLY DISCLAIMED. IN NO EVENT SHALL ATMEL BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * \asf_license_stop
 *
 */
/*
 * Support and FAQ: visit <a href="http://www.atmel.com/design-support/">Atmel Support</a>
 */

 /**
 * \mainpage
 * \section preface Preface
 * This is the reference manual for the Battery Service
 */
/****************************************************************************************
*							        Includes	                                     	*
****************************************************************************************/
#include "stddef.h"
#include "stdio.h"
#include "string.h"
#include "at_ble_api.h"
#include "ble_manager.h"
#include "pndcs.h"

/** Presentation format of the spectrum data */
at_ble_char_presentation_t presentation_format;

bool volatile pndcs_notification_flag = false;

extern ble_connected_dev_info_t ble_dev_info[BLE_MAX_DEVICE_CONNECTED];

pndcs_gatt_service_handler_t pndcs_serv;

/**@brief Initialize the service with its included service, characteristics, and descriptors
 */
at_ble_status_t pndcs_init_service(uint8_t *buf, uint16_t len)
{
	pndcs_serv.serv_handle = 0;
	pndcs_serv.serv_uuid.type = AT_BLE_UUID_128; //unique uuid
	memcpy(&pndcs_serv.serv_uuid.uuid[0], PNDCS_SERVICE_UUID, UUID_128BIT_LEN);
	
	//PNDCS service characteristic for transferring spectrum data
	pndcs_serv.serv_chars.char_val_handle = 0;          /* handle stored here */
	pndcs_serv.serv_chars.uuid.type = AT_BLE_UUID_128;
	memcpy(&pndcs_serv.serv_uuid.uuid[0], PNDCS_CHAR_DATA_UUID, UUID_128BIT_LEN);
	
	pndcs_serv.serv_chars.properties = (AT_BLE_CHAR_READ | AT_BLE_CHAR_NOTIFY); /* Properties */
	pndcs_serv.serv_chars.init_value = buf;             /* value */
	pndcs_serv.serv_chars.value_init_len = len;
	pndcs_serv.serv_chars.value_max_len = len;
#if BLE_PAIR_ENABLE
	pndcs_serv.serv_chars.value_permissions = (AT_BLE_ATTR_READABLE_REQ_AUTHN_NO_AUTHR |
												 AT_BLE_ATTR_WRITABLE_REQ_AUTHN_NO_AUTHR);   /* permissions */
#else
	pndcs_serv.serv_chars.value_permissions = AT_BLE_ATTR_NO_PERMISSIONS;   /* permissions */
#endif
	pndcs_serv.serv_chars.user_desc = NULL;           /* user defined name */
	pndcs_serv.serv_chars.user_desc_len = 0;
	pndcs_serv.serv_chars.user_desc_max_len = 0;
	pndcs_serv.serv_chars.user_desc_permissions = AT_BLE_ATTR_NO_PERMISSIONS;             /*user description permissions*/
	pndcs_serv.serv_chars.client_config_permissions = AT_BLE_ATTR_NO_PERMISSIONS;         /*client config permissions*/
	pndcs_serv.serv_chars.server_config_permissions = AT_BLE_ATTR_NO_PERMISSIONS;         /*server config permissions*/
	pndcs_serv.serv_chars.user_desc_handle = 0;             /*user desc handles*/
	pndcs_serv.serv_chars.client_config_handle = 0;         /*client config handles*/
	pndcs_serv.serv_chars.server_config_handle = 0;         /*server config handles*/
	
	presentation_format.format = AT_BLE_PRES_FORMAT_UINT8;
	presentation_format.exponent = PNDCS_CHAR_PRESENTATION_FORMAT_EXPONENT;
	presentation_format.unit = (uint8_t) PNDCS_CHAR_PRESENTATION_FORMAT_UNIT;
	presentation_format.unit = (uint8_t) (PNDCS_CHAR_PRESENTATION_FORMAT_UNIT >> 8);
	presentation_format.name_space = PNDCS_CHAR_PRESENTATION_FORMAT_NAMESPACE;
	presentation_format.description = (uint8_t) PNDCS_CHAR_PRESENTATION_FORMAT_DESCRIPTOR;
	presentation_format.description = (uint8_t) (PNDCS_CHAR_PRESENTATION_FORMAT_DESCRIPTOR >> 8);
	
	//pndcs_serv.serv_chars.presentation_format = &presentation_format;       /* presentation format */
	pndcs_serv.serv_chars.presentation_format = NULL;
	//ALL_UNUSED(pndcs_value);
	
	return pndcs_primary_service_define();
}

/**@brief defining a initialized service 
 */
at_ble_status_t pndcs_primary_service_define(void)
{
	DBG_LOG("defining primary service");
	return(at_ble_primary_service_define(&pndcs_serv.serv_uuid,
	&pndcs_serv.serv_handle,
	NULL, 0,
	&pndcs_serv.serv_chars, 1));
	return AT_BLE_SUCCESS;
}

/**@brief Function used to update characteristic value
 */
at_ble_status_t pndcs_update_char_value (at_ble_handle_t conn_handle, uint8_t *databuf, uint16_t datalen)
{
	at_ble_status_t status = AT_BLE_SUCCESS;
	
	/* Updating the att data base */
	if ((status = at_ble_characteristic_value_set(pndcs_serv.serv_chars.char_val_handle, databuf, datalen)) != AT_BLE_SUCCESS){
		DBG_LOG("updating the characteristic failed%d",status);
		return status;
	} else {
		DBG_LOG_DEV("updating the characteristic value is successful");
	}

	if(pndcs_notification_flag){
		/* sending notification to the peer about change in the battery level */ 
		if((status = at_ble_notification_send(conn_handle, pndcs_serv.serv_chars.char_val_handle)) != AT_BLE_SUCCESS) {
			DBG_LOG("sending notification failed%d",status);
			return status;
		}
		else {
			DBG_LOG_DEV("sending notification successful");
			//*flag = false;
			return status;
		}
	}
	return status;
}
/**@brief function to check the client characteristic configuration value. 
 */
at_ble_status_t pndcs_char_changed_event(at_ble_handle_t conn_handle, at_ble_characteristic_changed_t *char_handle, bool volatile *flag)
{
	at_ble_status_t status = AT_BLE_SUCCESS;
	at_ble_characteristic_changed_t change_params;
	memcpy((uint8_t *)&change_params, char_handle, sizeof(at_ble_characteristic_changed_t));
	
	if(pndcs_serv.serv_chars.client_config_handle == change_params.char_handle)
	{
		if(change_params.char_new_value[0])
		{
			pndcs_notification_flag = true;
			/* sending notification to the peer about change in the battery level */
			if((status = at_ble_notification_send(conn_handle, pndcs_serv.serv_chars.char_val_handle)) != AT_BLE_SUCCESS) {
				DBG_LOG("sending notification failed%d",status);
				return status;
			}
			else {
				DBG_LOG_DEV("sending notification successful");
				*flag = false;
				return status;
			}			
		}
		else
		{
			pndcs_notification_flag = false;			
		}
	}
	return status;
}
