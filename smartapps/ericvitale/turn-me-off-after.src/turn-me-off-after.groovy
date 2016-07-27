/**
 *  Turn Me Off After
 *
 *  1.0.0 - 07/11/16
 *   -- Initial Release
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *
 *  You can find this smart app @ https://github.com/ericvitale/ST-Trigger-My-Lights
 *  You can find my other device handlers & SmartApps @ https://github.com/ericvitale
 *
 */
 
definition(
	name: "Turn Me Off After",
	namespace: "ericvitale",
	author: "ericvitale@gmail.com",
	description: "Set on/off, level, color, and color temperature of a set of lights based on motion, acceleration, and a contact sensor.",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

preferences {
	page name: "mainPage"
}

def mainPage() {
	dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
    
    	section("Switches") {
			input "switches", "capability.switch", title: "Switches", multiple: true, required: false
    	}
        
        section("Schedule") {
        	input "timer", "number", title: "Turn Off After", required: true, defaultValue: 10
            input "unit", "enum", title: "Unit?", required: true, defaultValue: "Minutes", options: ["Seconds", "Minutes", "Hours", "Days"]
        	
        }
    
	    section([mobileOnly:true], "Options") {
			label(title: "Assign a name", required: false)
            input "active", "bool", title: "Rules Active?", required: true, defaultValue: true
            input "logging", "enum", title: "Log Level", required: true, defaultValue: "DEBUG", options: ["TRACE", "DEBUG", "INFO", "WARN", "ERROR"]
    	}
	}
}

private determineLogLevel(data) {
    switch (data?.toUpperCase()) {
        case "TRACE":
            return 0
            break
        case "DEBUG":
            return 1
            break
        case "INFO":
            return 2
            break
        case "WARN":
            return 3
            break
        case "ERROR":
        	return 4
            break
        default:
            return 1
    }
}

def log(data, type) {
    data = "TMOA -- ${data ?: ''}"
        
    if (determineLogLevel(type) >= determineLogLevel(settings?.logging ?: "INFO")) {
        switch (type?.toUpperCase()) {
            case "TRACE":
                log.trace "${data}"
                break
            case "DEBUG":
                log.debug "${data}"
                break
            case "INFO":
                log.info "${data}"
                break
            case "WARN":
                log.warn "${data}"
                break
            case "ERROR":
                log.error "${data}"
                break
            default:
                log.error "TMOA -- ${device.label} -- Invalid Log Setting"
        }
    }
}

def installed() {   
	log("Begin installed.", "DEBUG")
	initalization() 
    log("End installed.", "DEBUG")
}

def updated() {
	log("Begin updated().", "DEBUG")
	unsubscribe()
    unschedule()
	initalization()
    setAllLights("off")
    log("End updated().", "DEBUG")
}

def initalization() {
	log("Begin intialization().", "DEBUG")
    
    log("useTimer = ${useTimer}.", "INFO")
    log("active = ${active}.", "INFO")
    log("timer = ${timer}.", "INFO")
    log("unit = ${unit}.", "INFO")
    
    if(active) {
        subscribe(switches, "switch", switchHandler)
        log("Subscriptions to devices made.", "INFO") 
    } else {
    	log("App is set to inactive in settings.", "INFO")
    }

    log("End initialization().", "DEBUG")
}

def switchHandler(evt) {
	log("Begin switchHandler(evt).", "DEBUG")
	
    if(evt.value.toLowerCase() == "on") {
    	setSchedule()
    } else if (evt.value.toLowerCase() == "off") {
    	unschedule()
    } else {
    	log("Unhandled Event ${evt.value}.", "WARN")
    }
	
    log("End switchHandler(evt).", "DEBUG")
}
def setAllLightsOff() {
	log("Begin setAllLightsOff().", "DEBUG")
    	setAllLights("off")
        log("Turned lights off per the schedule.", "INFO")
    log("End setAllLightsOff().", "DEBUG")
}

def setAllLights(onOff) {
	log("Begin setAllLights(onOff)", "DEBUG")
    
    if(onOff.toLowerCase() == "off") {
        log("queue = ${state.queue}.", "DEBUG")
        switches?.off()
    } else {
    	switches?.on()
    }
    
    log("End setAllLights(onOff)", "DEBUG")
}

def setSchedule() {
	log("Begin setSchedule().", "DEBUG")
	runIn(determineDuration(), setAllLightsOff)
	log("Setting timer to turn off lights in ${timer} ${unit}.", "INFO")
    log("End setSchedule().", "DEBUG")
}

def getColorMap(val) {
	
    def colorMap = [:]
    
	switch(val.toLowerCase()) {
    	case "blue":
        	colorMap['hue'] = "240"
            colorMap['saturation'] = "100"
            colorMap['level'] = "50"
            break
        case "red":
        	colorMap['hue'] = "0"
            colorMap['saturation'] = "100"
            colorMap['level'] = "50"
            break
        case "yellow":
            colorMap['hue'] = "60"
            colorMap['saturation'] = "100"
            colorMap['level'] = "50"
        default:
            colorMap['hue'] = "60"
            colorMap['saturation'] = "100"
            colorMap['level'] = "50"	
    }
    
	return colorMap
}

def determineDuration() {
	switch (unit.toUpperCase()) {
    	case "SECONDS":
        	return timer
            break
        case "MINUTES":
            return timer * 60
            break
        case "HOURS":
           return timer * 60 * 60
           break
        case "DAYS":
           return timer * 60 * 60 * 24
           break
        default:
        	log("Invalid unit of ${unit}.", "ERROR")
            return unit    
     }
}