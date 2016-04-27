/**
 * \file
 *
 * \brief BLE Startup Template
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
 * Support and FAQ: visit <a href="http://www.atmel.com/design-support/">Atmel
 * Support</a>
 */

/**
 * \mainpage
 * \section preface Preface
 * This is the reference manual for the Startup Template
 */
/*- Includes ---------------------------------------------------------------*/
#include <asf.h>
#include "stdio.h"
#include "platform.h"
#include "at_ble_api.h"
#include "profiles.h"
#include "console_serial.h"
#include "timer_hw.h"
#include "conf_extint.h"
#include "conf_serialdrv.h"
#include "ble_manager.h"
#include "ble_utils.h"

#include "ble_pndcs.h"
#include "C:\Users\Anthony\Documents\Atmel Studio\7.0\STARTUP_TEMPLATE_SAML21_XPLAINED_PRO_B1\STARTUP_TEMPLATE_SAML21_XPLAINED_PRO_B1\src\ASF\thirdparty\wireless\ble_sdk\ble_services\pndcs\pndcs.h"
#include "C:\Users\Anthony\Documents\Atmel Studio\7.0\STARTUP_TEMPLATE_SAML21_XPLAINED_PRO_B1\STARTUP_TEMPLATE_SAML21_XPLAINED_PRO_B1\src\ASF\thirdparty\wireless\ble_sdk\ble_profiles\pndcsp\pndcsp.h"

#define PNDCS_NAME "PNDCS";

#define SPECTRUM_UPDATE_INTERVAL	(1) //1ms (adjust for different speeds), might need 200ms

uint8_t spectrum_data[APP_TX_BUF_SIZE];

pndcs_gatt_service_handler_t pndcs_data_service_handler;

bool volatile timer_cb_done = false;
bool volatile flag = true;
bool volatile data_flag = true;
at_ble_handle_t pndcs_connection_handle;

static at_ble_status_t start_advertisement(void)
{
	/* Start of advertisement */
	if(at_ble_adv_start(AT_BLE_ADV_TYPE_UNDIRECTED, AT_BLE_ADV_GEN_DISCOVERABLE, \
		NULL, AT_BLE_ADV_FP_ANY, APP_FAST_ADV, APP_ADV_TIMEOUT, 0) == \
		AT_BLE_SUCCESS)
	{
		DBG_LOG("BLE Started Advertisement");
		return AT_BLE_SUCCESS;
	}
	else
	{
		DBG_LOG("BLE Advertisement start Failed");
	}
	return AT_BLE_FAILURE;
}


/* Callback functions */ 

/**
* \Timer callback handler called on timer expiry
*/
static void timer_callback_handler(void)
{
	//Timer call back
	timer_cb_done = true;
}

/* Callback registered for AT_BLE_PAIR_DONE event from stack */
static at_ble_status_t ble_paired_app_event(void *param)
{
	timer_cb_done = false;
	hw_timer_start(SPECTRUM_UPDATE_INTERVAL);
	ALL_UNUSED(param);
	return AT_BLE_SUCCESS;
}

/* Callback registered for AT_BLE_DISCONNECTED event from stack */
static at_ble_status_t ble_disconnected_app_event(void *param)
{
	timer_cb_done = false;
	flag = true;
	hw_timer_stop();
	start_advertisement();
	ALL_UNUSED(param);
	return AT_BLE_SUCCESS;	
}

static at_ble_status_t ble_connected_app_event(void *param)
{
	//at_ble_connected_t *connected = (at_ble_connected_t *)param;
	//pndcs_connection_handle = connected->handle;
	#if !BLE_PAIR_ENABLE
	ble_paired_app_event(param);
	#else
	ALL_UNUSED(param);
	#endif
	return AT_BLE_SUCCESS;
}

/* Callback registered for AT_BLE_NOTIFICATION_CONFIRMED event from stack */
/*static at_ble_status_t ble_notification_confirmed_app_event(void *param)
{
	at_ble_cmd_complete_event_t *notification_status = (at_ble_cmd_complete_event_t *)param;
	if(!notification_status->status)
	{
		flag = true;
		DBG_LOG_DEV("sending notification to the peer success");
	}
	return AT_BLE_FAILURE;
}*/

/* Callback registered for AT_BLE_CHARACTERISTIC_CHANGED event from stack */
/*static at_ble_status_t ble_char_changed_app_event(void *param)
{
	at_ble_characteristic_changed_t *char_handle = (at_ble_characteristic_changed_t *)param;
	return pndcs_char_changed_event(char_handle->conn_handle, char_handle, &flag);
	ALL_UNUSED(param);
	return AT_BLE_SUCCESS;
}*/

static const ble_event_callback_t pndcs_app_gap_cb[] = {
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	ble_connected_app_event,
	ble_disconnected_app_event,
	NULL,
	NULL,
	ble_paired_app_event,
	NULL,
	NULL,
	NULL,
	NULL,
	ble_paired_app_event,
	NULL,
	NULL,
	NULL,
	NULL
};

/*static const ble_event_callback_t pndcs_app_gatt_server_cb[] = {
	ble_notification_confirmed_app_event,
	NULL,
	ble_char_changed_app_event,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL,
	NULL
};*/

void button_cb(void)
{
	//debugging button
	port_pin_toggle_output_level(EXT3_PIN_GPIO_0);
}

/* Function used for send data */

static void pndcs_app_send_data(void)
{
	//clear buffer
	memset(&spectrum_data[0], 0, sizeof(spectrum_data));
	
	//replace file read operations with input from spectrometer and uart
	FILE *fp;
	float pixel;

	fp = fopen("data_txt.txt", "r");

	if (fp == NULL) {
		DBG_LOG("Can't open data file!");
	}
	
	//skip first line
	fscanf(fp, "%*[^\n]\n", NULL);
	
	//temp variables
	char count = 0; //count for number of values read into buffer
	uint8_t component = 0; //x or y component
	int i = 0; //index of spectrum data
	
	//send start command
	spectrum_data[0] = START_COMMAND;
	pndcs_prf_send_data(spectrum_data, 1);
	
	//read data and send to phone (alternates between x (MSB=0) and y (MSB=1) components)
	fscanf(fp, "%f", &pixel); //read first value(x component)
	while (1) 
	{
		/**if (timer_cb_done)
		{
			if needed for timing 
		}*/
		
		//concatenate value to spectrum_data
		if(count != APP_TX_BUF_SIZE/5)
		{
			uint8_t *array;
			array = (uint8_t*)(&pixel);
			
			spectrum_data[i] = component;
			spectrum_data[i+1] = array[0];   
			spectrum_data[i+2] = array[1];    
			spectrum_data[i+3] = array[2];   
			spectrum_data[i+4] = array[3];   
			i+=5;
			
			if(component == 0) component = 1;
			else component = 0;
			
			count++;
		}	
		//send filled buffer data
		else if(count == APP_TX_BUF_SIZE/5)
		{
			pndcs_prf_send_data(spectrum_data, APP_TX_BUF_SIZE);
			count = 0;
			memset(&spectrum_data[0], 0, sizeof(spectrum_data));
			i = 0;
		}
		
		//if next value is end of file send and break
		else if(fscanf(fp, "%f", &pixel) != 1)
		{
			pndcs_prf_send_data(spectrum_data, i); //maybe i+1?
			count = 0;
			memset(&spectrum_data[0], 0, sizeof(spectrum_data));
			i = 0;
			break;
		}
	}
		
	//send end command
	spectrum_data[0] = END_COMMAND;
	pndcs_prf_send_data(spectrum_data, 1);
}


int main(void)
{
	#if SAMG55 || SAM4S
	/* Initialize the SAM system. */
	sysclk_init();
	board_init();
	#elif SAM0
	system_init();
	#endif
	
	/* Initialize serial console */
	serial_console_init();
	
	/* Hardware timer */
	hw_timer_init();
	
	/* button initialization */
	button_init();
	
	hw_timer_register_callback(timer_callback_handler);

	DBG_LOG("Initializing BLE Application");
		
	/* initialize the BLE chip  and Set the Device Address */
	ble_device_init(NULL);
	
	pndcs_prf_buf_init(&spectrum_data[0], APP_TX_BUF_SIZE);
	
	/* Register Primary/Included service in case of GATT Server */
	pndcs_prf_init(NULL);
	
	/* Start the advertisement */
	start_advertisement();
	
	/* Register callbacks for gap related events */ //(TODO)
	ble_mgr_events_callback_handler(REGISTER_CALL_BACK,
									BLE_GAP_EVENT_TYPE,
									pndcs_app_gap_cb);
	
	/* Register callbacks for gatt server related events */ //(TODO)
	//ble_mgr_events_callback_handler(REGISTER_CALL_BACK,
									//BLE_GATT_SERVER_EVENT_TYPE,
									//pndcs_app_gatt_server_cb);
	
	
	while(true)
	{
		ble_event_task();
	}
	

}

