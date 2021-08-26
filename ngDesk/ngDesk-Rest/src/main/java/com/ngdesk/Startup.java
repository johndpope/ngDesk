package com.ngdesk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class Startup implements ApplicationListener<ApplicationReadyEvent> {

	@Value("${env}")
	private String environment;

	@Autowired
	private Global global; 

	private final Logger log = LoggerFactory.getLogger(Startup.class);

	@Override
	public void onApplicationEvent(ApplicationReadyEvent arg0) {

		log.trace("Enter Startup");

		try {

			String[] timeZones = new String[] { "Africa/Abidjan", "Africa/Accra", "Africa/Addis_Ababa",
					"Africa/Algiers", "Africa/Asmara", "Africa/Asmera", "Africa/Bamako", "Africa/Bangui",
					"Africa/Banjul", "Africa/Bissau", "Africa/Blantyre", "Africa/Brazzaville", "Africa/Bujumbura",
					"Africa/Cairo", "Africa/Casablanca", "Africa/Ceuta", "Africa/Conakry", "Africa/Dakar",
					"Africa/Dar_es_Salaam", "Africa/Djibouti", "Africa/Douala", "Africa/El_Aaiun", "Africa/Freetown",
					"Africa/Gaborone", "Africa/Harare", "Africa/Johannesburg", "Africa/Juba", "Africa/Kampala",
					"Africa/Khartoum", "Africa/Kigali", "Africa/Kinshasa", "Africa/Lagos", "Africa/Libreville",
					"Africa/Lome", "Africa/Luanda", "Africa/Lubumbashi", "Africa/Lusaka", "Africa/Malabo",
					"Africa/Maputo", "Africa/Maseru", "Africa/Mbabane", "Africa/Mogadishu", "Africa/Monrovia",
					"Africa/Nairobi", "Africa/Ndjamena", "Africa/Niamey", "Africa/Nouakchott", "Africa/Ouagadougou",
					"Africa/Porto-Novo", "Africa/Sao_Tome", "Africa/Timbuktu", "Africa/Tripoli", "Africa/Tunis",
					"Africa/Windhoek", "America/Adak", "America/Anchorage", "America/Anguilla", "America/Antigua",
					"America/Araguaina", "America/Argentina/Buenos_Aires", "America/Argentina/Catamarca",
					"America/Argentina/ComodRivadavia", "America/Argentina/Cordoba", "America/Argentina/Jujuy",
					"America/Argentina/La_Rioja", "America/Argentina/Mendoza", "America/Argentina/Rio_Gallegos",
					"America/Argentina/Salta", "America/Argentina/San_Juan", "America/Argentina/San_Luis",
					"America/Argentina/Tucuman", "America/Argentina/Ushuaia", "America/Aruba", "America/Asuncion",
					"America/Atikokan", "America/Atka", "America/Bahia", "America/Bahia_Banderas", "America/Barbados",
					"America/Belem", "America/Belize", "America/Blanc-Sablon", "America/Boa_Vista", "America/Bogota",
					"America/Boise", "America/Buenos_Aires", "America/Cambridge_Bay", "America/Campo_Grande",
					"America/Cancun", "America/Caracas", "America/Catamarca", "America/Cayenne", "America/Cayman",
					"America/Chicago", "America/Chihuahua", "America/Coral_Harbour", "America/Cordoba",
					"America/Costa_Rica", "America/Creston", "America/Cuiaba", "America/Curacao",
					"America/Danmarkshavn", "America/Dawson", "America/Dawson_Creek", "America/Denver",
					"America/Detroit", "America/Dominica", "America/Edmonton", "America/Eirunepe",
					"America/El_Salvador", "America/Ensenada", "America/Fort_Nelson", "America/Fort_Wayne",
					"America/Fortaleza", "America/Glace_Bay", "America/Godthab", "America/Goose_Bay",
					"America/Grand_Turk", "America/Grenada", "America/Guadeloupe", "America/Guatemala",
					"America/Guayaquil", "America/Guyana", "America/Halifax", "America/Havana", "America/Hermosillo",
					"America/Indiana/Indianapolis", "America/Indiana/Knox", "America/Indiana/Marengo",
					"America/Indiana/Petersburg", "America/Indiana/Tell_City", "America/Indiana/Vevay",
					"America/Indiana/Vincennes", "America/Indiana/Winamac", "America/Indianapolis", "America/Inuvik",
					"America/Iqaluit", "America/Jamaica", "America/Jujuy", "America/Juneau",
					"America/Kentucky/Louisville", "America/Kentucky/Monticello", "America/Knox_IN",
					"America/Kralendijk", "America/La_Paz", "America/Lima", "America/Los_Angeles", "America/Louisville",
					"America/Lower_Princes", "America/Maceio", "America/Managua", "America/Manaus", "America/Marigot",
					"America/Martinique", "America/Matamoros", "America/Mazatlan", "America/Mendoza",
					"America/Menominee", "America/Merida", "America/Metlakatla", "America/Mexico_City",
					"America/Miquelon", "America/Moncton", "America/Monterrey", "America/Montevideo",
					"America/Montreal", "America/Montserrat", "America/Nassau", "America/New_York", "America/Nipigon",
					"America/Nome", "America/Noronha", "America/North_Dakota/Beulah", "America/North_Dakota/Center",
					"America/North_Dakota/New_Salem", "America/Ojinaga", "America/Panama", "America/Pangnirtung",
					"America/Paramaribo", "America/Phoenix", "America/Port-au-Prince", "America/Port_of_Spain",
					"America/Porto_Acre", "America/Porto_Velho", "America/Puerto_Rico", "America/Punta_Arenas",
					"America/Rainy_River", "America/Rankin_Inlet", "America/Recife", "America/Regina",
					"America/Resolute", "America/Rio_Branco", "America/Rosario", "America/Santa_Isabel",
					"America/Santarem", "America/Santiago", "America/Santo_Domingo", "America/Sao_Paulo",
					"America/Scoresbysund", "America/Shiprock", "America/Sitka", "America/St_Barthelemy",
					"America/St_Johns", "America/St_Kitts", "America/St_Lucia", "America/St_Thomas",
					"America/St_Vincent", "America/Swift_Current", "America/Tegucigalpa", "America/Thule",
					"America/Thunder_Bay", "America/Tijuana", "America/Toronto", "America/Tortola", "America/Vancouver",
					"America/Virgin", "America/Whitehorse", "America/Winnipeg", "America/Yakutat",
					"America/Yellowknife", "Antarctica/Casey", "Antarctica/Davis", "Antarctica/DumontDUrville",
					"Antarctica/Macquarie", "Antarctica/Mawson", "Antarctica/McMurdo", "Antarctica/Palmer",
					"Antarctica/Rothera", "Antarctica/South_Pole", "Antarctica/Syowa", "Antarctica/Troll",
					"Antarctica/Vostok", "Arctic/Longyearbyen", "Asia/Aden", "Asia/Almaty", "Asia/Amman", "Asia/Anadyr",
					"Asia/Aqtau", "Asia/Aqtobe", "Asia/Ashgabat", "Asia/Ashkhabad", "Asia/Atyrau", "Asia/Baghdad",
					"Asia/Bahrain", "Asia/Baku", "Asia/Bangkok", "Asia/Barnaul", "Asia/Beirut", "Asia/Bishkek",
					"Asia/Brunei", "Asia/Calcutta", "Asia/Chita", "Asia/Choibalsan", "Asia/Chongqing", "Asia/Chungking",
					"Asia/Colombo", "Asia/Dacca", "Asia/Damascus", "Asia/Dhaka", "Asia/Dili", "Asia/Dubai",
					"Asia/Dushanbe", "Asia/Famagusta", "Asia/Gaza", "Asia/Harbin", "Asia/Hebron", "Asia/Ho_Chi_Minh",
					"Asia/Hong_Kong", "Asia/Hovd", "Asia/Irkutsk", "Asia/Istanbul", "Asia/Jakarta", "Asia/Jayapura",
					"Asia/Jerusalem", "Asia/Kabul", "Asia/Kamchatka", "Asia/Karachi", "Asia/Kashgar", "Asia/Kathmandu",
					"Asia/Katmandu", "Asia/Khandyga", "Asia/Kolkata", "Asia/Krasnoyarsk", "Asia/Kuala_Lumpur",
					"Asia/Kuching", "Asia/Kuwait", "Asia/Macao", "Asia/Macau", "Asia/Magadan", "Asia/Makassar",
					"Asia/Manila", "Asia/Muscat", "Asia/Nicosia", "Asia/Novokuznetsk", "Asia/Novosibirsk", "Asia/Omsk",
					"Asia/Oral", "Asia/Phnom_Penh", "Asia/Pontianak", "Asia/Pyongyang", "Asia/Qatar", "Asia/Qyzylorda",
					"Asia/Rangoon", "Asia/Riyadh", "Asia/Saigon", "Asia/Sakhalin", "Asia/Samarkand", "Asia/Seoul",
					"Asia/Shanghai", "Asia/Singapore", "Asia/Srednekolymsk", "Asia/Taipei", "Asia/Tashkent",
					"Asia/Tbilisi", "Asia/Tehran", "Asia/Tel_Aviv", "Asia/Thimbu", "Asia/Thimphu", "Asia/Tokyo",
					"Asia/Tomsk", "Asia/Ujung_Pandang", "Asia/Ulaanbaatar", "Asia/Ulan_Bator", "Asia/Urumqi",
					"Asia/Ust-Nera", "Asia/Vientiane", "Asia/Vladivostok", "Asia/Yakutsk", "Asia/Yangon",
					"Asia/Yekaterinburg", "Asia/Yerevan", "Atlantic/Azores", "Atlantic/Bermuda", "Atlantic/Canary",
					"Atlantic/Cape_Verde", "Atlantic/Faeroe", "Atlantic/Faroe", "Atlantic/Jan_Mayen",
					"Atlantic/Madeira", "Atlantic/Reykjavik", "Atlantic/South_Georgia", "Atlantic/St_Helena",
					"Atlantic/Stanley", "Australia/ACT", "Australia/Adelaide", "Australia/Brisbane",
					"Australia/Broken_Hill", "Australia/Canberra", "Australia/Currie", "Australia/Darwin",
					"Australia/Eucla", "Australia/Hobart", "Australia/LHI", "Australia/Lindeman", "Australia/Lord_Howe",
					"Australia/Melbourne", "Australia/NSW", "Australia/North", "Australia/Perth",
					"Australia/Queensland", "Australia/South", "Australia/Sydney", "Australia/Tasmania",
					"Australia/Victoria", "Australia/West", "Australia/Yancowinna", "Brazil/Acre", "Brazil/DeNoronha",
					"Brazil/East", "Brazil/West", "Canada/Atlantic", "Canada/Central", "Canada/East-Saskatchewan",
					"Canada/Eastern", "Canada/Mountain", "Canada/Newfoundland", "Canada/Pacific", "Canada/Saskatchewan",
					"Canada/Yukon", "Chile/Continental", "Chile/EasterIsland", "Cuba", "Egypt", "Eire",
					"Europe/Amsterdam", "Europe/Andorra", "Europe/Astrakhan", "Europe/Athens", "Europe/Belfast",
					"Europe/Belgrade", "Europe/Berlin", "Europe/Bratislava", "Europe/Brussels", "Europe/Bucharest",
					"Europe/Budapest", "Europe/Busingen", "Europe/Chisinau", "Europe/Copenhagen", "Europe/Dublin",
					"Europe/Gibraltar", "Europe/Guernsey", "Europe/Helsinki", "Europe/Isle_of_Man", "Europe/Istanbul",
					"Europe/Jersey", "Europe/Kaliningrad", "Europe/Kiev", "Europe/Kirov", "Europe/Lisbon",
					"Europe/Ljubljana", "Europe/London", "Europe/Luxembourg", "Europe/Madrid", "Europe/Malta",
					"Europe/Mariehamn", "Europe/Minsk", "Europe/Monaco", "Europe/Moscow", "Europe/Nicosia",
					"Europe/Oslo", "Europe/Paris", "Europe/Podgorica", "Europe/Prague", "Europe/Riga", "Europe/Rome",
					"Europe/Samara", "Europe/San_Marino", "Europe/Sarajevo", "Europe/Saratov", "Europe/Simferopol",
					"Europe/Skopje", "Europe/Sofia", "Europe/Stockholm", "Europe/Tallinn", "Europe/Tirane",
					"Europe/Tiraspol", "Europe/Ulyanovsk", "Europe/Uzhgorod", "Europe/Vaduz", "Europe/Vatican",
					"Europe/Vienna", "Europe/Vilnius", "Europe/Volgograd", "Europe/Warsaw", "Europe/Zagreb",
					"Europe/Zaporozhye", "Europe/Zurich", "Greenwich", "Hongkong", "Iceland", "Indian/Antananarivo",
					"Indian/Chagos", "Indian/Christmas", "Indian/Cocos", "Indian/Comoro", "Indian/Kerguelen",
					"Indian/Mahe", "Indian/Maldives", "Indian/Mauritius", "Indian/Mayotte", "Indian/Reunion", "Iran",
					"Israel", "Jamaica", "Japan", "Kwajalein", "Libya", "Mexico/BajaNorte", "Mexico/BajaSur",
					"Mexico/General", "Pacific/Apia", "Pacific/Auckland", "Pacific/Bougainville", "Pacific/Chatham",
					"Pacific/Chuuk", "Pacific/Easter", "Pacific/Efate", "Pacific/Enderbury", "Pacific/Fakaofo",
					"Pacific/Fiji", "Pacific/Funafuti", "Pacific/Galapagos", "Pacific/Gambier", "Pacific/Guadalcanal",
					"Pacific/Guam", "Pacific/Honolulu", "Pacific/Johnston", "Pacific/Kiritimati", "Pacific/Kosrae",
					"Pacific/Kwajalein", "Pacific/Majuro", "Pacific/Marquesas", "Pacific/Midway", "Pacific/Nauru",
					"Pacific/Niue", "Pacific/Norfolk", "Pacific/Noumea", "Pacific/Pago_Pago", "Pacific/Palau",
					"Pacific/Pitcairn", "Pacific/Pohnpei", "Pacific/Ponape", "Pacific/Port_Moresby",
					"Pacific/Rarotonga", "Pacific/Saipan", "Pacific/Samoa", "Pacific/Tahiti", "Pacific/Tarawa",
					"Pacific/Tongatapu", "Pacific/Truk", "Pacific/Wake", "Pacific/Wallis", "Pacific/Yap", "Poland",
					"Portugal", "Singapore", "Turkey", "US/Alaska", "US/Aleutian", "US/Arizona", "US/Central",
					"US/East-Indiana", "US/Eastern", "US/Hawaii", "US/Indiana-Starke", "US/Michigan", "US/Mountain",
					"US/Pacific", "US/Pacific-New", "US/Samoa" };
			String[] locales = { "af-NA", "af", "ak", "sq", "am", "ar-DZ", "ar-BH", "ar-EG", "ar-IQ", "ar-JO", "ar-KW",
					"ar-LB", "ar-LY", "ar-MA", "ar-OM", "ar-QA", "ar-SA", "ar-SD", "ar-SY", "ar-TN", "ar-AE", "ar-YE",
					"ar", "hy", "as", "asa", "az-Cyrl", "az-Latn", "az", "bm-ML", "bm", "eu-ES", "eu", "be-BY", "be",
					"bem-ZM", "bem", "bez-TZ", "bez", "bn-IN", "bn", "bs", "bg-BG", "bg", "my", "yue-Hant-HK", "ca-ES",
					"ca", "tzm-Latn", "tzm-Latn-MA", "tzm", "chr-US", "chr", "cgg-UG", "cgg", "zh-Hans", "zh-Hans-CN",
					"zh-Hans-HK", "zh-Hans-MO", "zh-Hans-SG", "zh-Hant", "zh-Hant-HK", "zh-Hant-MO", "zh-Hant-TW", "zh",
					"kw-GB", "kw", "hr-HR", "hr", "cs-CZ", "cs", "da-DK", "da", "nl-BE", "nl-NL", "nl", "ebu-KE", "ebu",
					"en-AS", "en-AU", "en-BE", "en-BZ", "en-BW", "en-CA", "en-GU", "en-HK", "en-IN", "en-IE", "en-IL",
					"en-JM", "en-MT", "en-MH", "en-MU", "en-NA", "en-NZ", "en-MP", "en-PK", "en-PH", "en-SG", "en-ZA",
					"en-TT", "en-UM", "en-VI", "en-GB", "en-US", "en-ZW", "en", "eo", "et-EE", "et", "ee-GH", "ee-TG",
					"ee", "fo-FO", "fo", "fil-PH", "fil", "fi-FI", "fi", "fr-BE", "fr-BJ", "fr-BF", "fr-BI", "fr-CM",
					"fr-CA", "fr-CF", "fr-TD", "fr-KM", "fr-CG", "fr-CD", "fr-CI", "fr-DJ", "fr-GQ", "fr-FR", "fr-GA",
					"fr-GP", "fr-GN", "fr-LU", "fr-MG", "fr-ML", "fr-MQ", "fr-MC", "fr-NE", "fr-RW", "fr-RE", "fr-BL",
					"fr-MF", "fr-SN", "fr-CH", "fr-TG", "fr", "ff-SN", "ff", "gl-ES", "gl", "lg-UG", "lg", "ka-GE",
					"ka", "de-AT", "de-BE", "de-DE", "de-LI", "de-LU", "de-CH", "de", "el-CY", "el-GR", "el", "gu-IN",
					"gu", "guz-KE", "guz", "ha-Latn", "ha-Latn-GH", "ha-Latn-NE", "ha-Latn-NG", "ha", "haw-US", "haw",
					"he-IL", "he", "hi-IN", "hi", "hu-HU", "hu", "is-IS", "is", "ig-NG", "ig", "id-ID", "id", "ga-IE",
					"ga", "it-IT", "it-CH", "it", "ja-JP", "ja", "kea-CV", "kea", "kab-DZ", "kab", "kl-GL", "kl",
					"kln-KE", "kln", "kam-KE", "kam", "kn-IN", "kn", "kk-Cyrl", "kk-Cyrl-KZ", "kk", "km-KH", "km",
					"ki-KE", "ki", "rw-RW", "rw", "kok-IN", "kok", "ko-KR", "ko", "khq-ML", "khq", "ses-ML", "ses",
					"lag-TZ", "lag", "lv-LV", "lv", "lt-LT", "lt", "luo-KE", "luo", "luy-KE", "luy", "mk-MK", "mk",
					"jmc-TZ", "jmc", "kde-TZ", "kde", "mg-MG", "mg", "ms-BN", "ms-MY", "ms", "ml-IN", "ml", "mt-MT",
					"mt", "gv-GB", "gv", "mr-IN", "mr", "mas-KE", "mas-TZ", "mas", "mer-KE", "mer", "mfe-MU", "mfe",
					"naq-NA", "naq", "ne-IN", "ne-NP", "ne", "nd-ZW", "nd", "nb-NO", "nb", "nn-NO", "nn", "nyn-UG",
					"nyn", "or-IN", "or", "om-ET", "om-KE", "om", "ps-AF", "ps", "fa-AF", "fa-IR", "fa", "pl-PL", "pl",
					"pt-BR", "pt-GW", "pt-MZ", "pt-PT", "pt", "pa-Arab", "pa-Arab-PK", "pa-Guru", "pa-Guru-IN", "pa",
					"ro-MD", "ro-RO", "ro", "rm-CH", "rm", "rof-TZ", "rof", "ru-MD", "ru-RU", "ru-UA", "ru", "rwk-TZ",
					"rwk", "saq-KE", "saq", "sg-CF", "sg", "seh-MZ", "seh", "sr-Cyrl", "sr-Cyrl-BA", "sr-Cyrl-ME",
					"sr-Cyrl-RS", "sr-Latn", "sr-Latn-BA", "sr-Latn-ME", "sr-Latn-RS", "sr", "sn-ZW", "sn", "ii-CN",
					"ii", "si-LK", "si", "sk-SK", "sk", "sl-SI", "sl", "xog-UG", "xog", "so-DJ", "so-ET", "so-KE",
					"so-SO", "so", "es-AR", "es-BO", "es-CL", "es-CO", "es-CR", "es-DO", "es-EC", "es-SV", "es-GQ",
					"es-GT", "es-HN", "es-419", "es-MX", "es-NI", "es-PA", "es-PY", "es-PE", "es-PR", "es-ES", "es-US",
					"es-UY", "es-VE", "es", "sw-KE", "sw-TZ", "sw", "sv-FI", "sv-SE", "sv", "gsw-CH", "gsw", "shi-Latn",
					"shi-Latn-MA", "shi-Tfng", "shi-Tfng-MA", "shi", "dav-KE", "dav", "ta-IN", "ta-LK", "ta", "te-IN",
					"te", "teo-KE", "teo-UG", "teo", "th-TH", "th", "bo-CN", "bo-IN", "bo", "ti-ER", "ti-ET", "ti",
					"to-TO", "to", "tr-TR", "tr", "uk-UA", "uk", "ur-IN", "ur-PK", "ur", "uz-Arab", "uz-Arab-AF",
					"uz-Cyrl", "uz-Cyrl-UZ", "uz-Latn", "uz-Latn-UZ", "uz", "vi-VN", "vi", "vun-TZ", "vun", "cy-GB",
					"cy", "yo", "zu-ZA", "zu"

			};
			String[] times = new String[] { "00:00", "00:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30",
					"04:00", "04:30", "05:00", "05:30", "06:00", "06:30", "07:00", "07:30", "08:00", "08:30", "09:00",
					"09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
					"15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00",
					"20:30", "21:00", "21:30", "22:00", "22:30", "23:00", "23:30" };
			String[] displayDatatypes = new String[] { "Auto Number", "Formula", "Relationship", "Currency", "Email",
					"Geolocation", "Number", "Percent", "Phone", "Picklist", "Picklist (Multi-Select)", "Text",
					"Text Area", "Text Area Long", "Text Area Rich", "Time", "URL", "File Upload", "Payment",
					"Discussion", "Checkbox", "Date", "Date/Time", "Street 1", "Street 2", "City", "Country", "State",
					"Zipcode", "List Text", "Chronometer", "ID", "Country", "Formula", "Address", "Button",
					"Workflow Stages", "Aggregate", "Approval", "File Preview" };

			String[] backendDataTypes = new String[] { "String", "Array", "Float", "json", "Integer", "Double",
					"Timestamp", "Boolean", "Date", "BLOB", "Formula", "Button", "Aggregate", "Approval" };

			String[] reportingOperators = new String[] { "CONTAINS", "EQUALS_TO", "NOT_EQUALS_TO", "DOES_NOT_CONTAIN",
					"GREATER_THAN", "LESS_THAN", "REGEX", "DAYS_BEFORE_TODAY" };

			String[] operators = new String[] { "GREATER_THAN", "LESS_THAN", "EQUALS_TO", "NOT_EQUALS_TO", "REGEX",
					"DOES_NOT_CONTAIN", "CONTAINS", "IS_UNIQUE", "CHANGED", "EXISTS", "DOES_NOT_EXIST",
					"LENGTH_IS_GREATER_THAN", "LENGTH_IS_LESS_THAN" };

			String[] chatPromptOperators = new String[] { "EQUALS_TO", "NOT_EQUALS_TO", "GREATER_THAN", "LESS_THAN",
					"REGEX", "DOES_NOT_CONTAIN", "CONTAINS" };

			String[] chatPromptConditions = new String[] { "HOUR_OF_DAY", "DAY_OF_WEEK", "STILL_ON_PAGE",
					"STILL_ON_SITE", "VISITOR_PAGE_URL", "VISITOR_PAGE_TITLE" };

			String[] chatPromptTriggers = new String[] { "LOADED_CHAT_WIDGET", "REQUESTS_CHAT", "MESSAGE_SENT" };

			String[] slaOperators = new String[] { "GREATER_THAN", "LESS_THAN", "EQUALS_TO", "NOT_EQUALS_TO", "REGEX",
					"DOES_NOT_CONTAIN", "CONTAINS", "IS_UNIQUE", "EXISTS", "DOES_NOT_EXIST", "LENGTH_IS_GREATER_THAN",
					"LENGTH_IS_LESS_THAN" };

			String[] slaViolationOperators = new String[] { "HAS_BEEN", "HAS_NOT_CHANGED", "HAS_NOT_BEEN_REPLIED_BY",
					"IS_PAST_BY", "IS_WITHIN" };

			// TO CHECK THE VALID OPERATORS FOR FIELDS BASED ON DATA TYPE

			String[] validTextOperators = { "EQUALS_TO", "NOT_EQUALS_TO", "IS", "REGEX", "DOES_NOT_CONTAIN", "CONTAINS",
					"LENGTH_IS_GREATER_THAN", "LENGTH_IS_LESS_THAN" };
			String[] validNumericOperators = { "GREATER_THAN", "LESS_THAN", "EQUALS_TO", "NOT_EQUALS_TO" };
			String[] validDateOperators = { "GREATER_THAN", "LESS_THAN", "EXISTS", "DOES_NOT_EXIST" };
			String[] validRelationOperators = { "EQUALS_TO", "NOT_EQUALS_TO" };

			String[] actions = new String[] { "send email", "start escalation", "stop escalation" };
			String[] primaryColors = new String[] { "#3f51b5", "#43a047", "#f44336", "#ffea00", "#9c27b0", "#000000",
					"#f90200" };
			String[] secondaryColors = new String[] { "#e8eaf6", "#e8f5e9", "#ffebee", "#fffde7", "#f3e5f5", "#cccccc",
					"#fbfaff" };
			String[] chatTriggerTypes = new String[] { "Loaded the chat widget", "Request a chat",
					"Chat message sent" };
			String[] chatTriggerStringOperators = new String[] { "equals to", "not equals to", "contains",
					"does not contain", "regex", "is unique" };
			String[] chatTriggerIntegerOperators = new String[] { "equals to", "not equals to", "less than",
					"greater than" };
			String[] chatTriggerConditions = new String[] { "HOUR_OF_DAY", "DAY_OF_WEEK", "STILL_ON_SITE",
					"STILL_ON_PAGE", "IP_ADDRESS", "HOSTNAME", "CITY", "COUNTRY_NAME", "NAME", "EMAIL_ADDRESS",
					"REFERRER", "FULL_URL", "PAGE_TITLE", "USER_AGENT", "BROWSER", "PLATFORM", "QUEUE_SIZE" };
			String[] chatTriggerActions = new String[] { "Set triggered", "Wait", "Set name of visitor",
					"Send message to visitor" };
			String[] nodeTypes = new String[] { "Route", "CreateEntry", "Javascript", "HttpRequest", "Say",
					"SayAndGather", "CreateEntryAndAssign", "GetEntries", "SendEmail", "UpdateEntry", "DeleteEntry",
					"Start", "StartEscalation", "StopEscalation", "MakePhoneCall", "SendSms", "FindAgentAndAssign",
					"ChatBot" };

			String[] chatVariables = new String[] { "BROWSER", "USER_AGENT", "DEVICE", "URL", "PLATFORM", "HOSTNAME",
					"IP_ADDRESS", "PAGE_TITLE", "USER", "TIME_OF_DAY", "DAY_OF_WEEK" };
			String[] emailVariables = new String[] { "TO", "FROM", "SUBJECT", "CC", "BCC", "BODY", "TIME_OF_DAY",
					"DAY_OF_WEEK" };
			String[] userNotifications = new String[] { "wall_clock_alarm", "alarm_classic", "bellchime",
					"burglar_alarm" };
			String[] restrictedSubdomains = new String[] { "api", "www", "test", "tst", "qa", "download", "downloads",
					"public", "private", "stg", "stage", "signup", "developer", "sso", "ngdesk", "mail", "analytics",
					"dev", "prd", "inbound-email", "cdn", "voip" };
			String[] restrictedFieldNames = new String[] { "DATE_CREATED", "DATE_UPDATED", "LAST_UPDATED_BY",
					"CREATED_BY" };
			// TODO: FILL LIST
			String[] intervalVariables = new String[] { "SOURCE_TYPE" };

			String[] restrictedModuleNames = new String[] { "Accounts", "Tickets", "Users", "Teams", "Live Chat" };
			String[] industries = { "Accounting", "Airlines", "Apparel/Fashion", "Architecture", "Automobile",
					"Arts/Crafts", "Banking", "Media", "Technology", "Other", "Chemicals", "Education", "Engineering",
					"Networking", "Dairy", "Agriculture", "Entertainment", "Food", "Health/Fitness", "Medical" };
			String[] departments = { "Production", "Research and Development", "Sales", "Marketing",
					"Human Resource Management", "Operations", "Accounting and Finance" };
			String[] companySizes = { "1 - 45 employees", "46 - 200 employees", "201 - 1,000 employees",
					"1,001 - 4,500 employees", "4,501 - 10,000 employees", "10,000 + employees" };

			String[] defaultModules = { "Users", "Accounts", "Categories", "Sections", "Articles", "Tickets", "Teams" };
			String[] supportedLanguages = { "ar", "de", "el", "en", "es", "fr", "hi", "it", "ms", "pt", "ru", "zh",
					"no" };
			String[] walkthroughApiKeys = { "TICKETS_LIST", "CHAT_DETAIL", "TICKETS_DETAIL", "CHAT_WIDGETS_LIST" };
			String[] deletionFeedback = { "TROUBLE_GETTING_STARTED", "CREATED_SECOND_ACCOUNT", "PRIVACY_CONCERNS",
					"WANT_TO_REMOVE_SOMETHING", "TOO_BUSY_TOO_DISTRACTING", "TOO_DIFFICULT_TO_USE", "SOMETHING_ELSE" };

			String[] validInstallerPlatforms = { "windows", "linux", "osx", "windows-x64", "linux-x64" };

			global.walkthroughApiKeys = Arrays.asList(walkthroughApiKeys);
			global.postHeaders = new HttpHeaders();
			global.postHeaders.setContentType(MediaType.APPLICATION_JSON);

			global.companySizes = (List) Arrays.asList(companySizes);
			global.departments = (List) Arrays.asList(departments);
			global.industries = (List) Arrays.asList(industries);
			global.times = (List) Arrays.asList(times);
			global.timezones = (List) Arrays.asList(timeZones);
			global.locale = Arrays.asList(locales);
			global.displayDataTypes = (List) Arrays.asList(displayDatatypes);
			global.backendDataTypes = (List) Arrays.asList(backendDataTypes);
			global.reportingOperators = (List) Arrays.asList(reportingOperators);
			global.operators = (List) Arrays.asList(operators);
			global.actions = (List) Arrays.asList(actions);
			global.nodeTypes = (List) Arrays.asList(nodeTypes);

			global.chatVariables = (List) Arrays.asList(chatVariables);
			global.emailVariables = (List) Arrays.asList(emailVariables);
			global.intervalVariables = (List) Arrays.asList(intervalVariables);

			global.postHeaders = new HttpHeaders();
			global.postHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
			global.primaryColors = (List) Arrays.asList(primaryColors);
			global.secondaryColors = (List) Arrays.asList(secondaryColors);
			global.chatTriggerTypes = (List) Arrays.asList(chatTriggerTypes);
			global.chatTriggerStringOperators = (List) Arrays.asList(chatTriggerStringOperators);
			global.chatTriggerIntegerOperators = (List) Arrays.asList(chatTriggerIntegerOperators);
			global.chatTriggerConditions = (List) Arrays.asList(chatTriggerConditions);
			global.chatTriggerActions = (List) Arrays.asList(chatTriggerActions);

			global.userNotifications = (List) Arrays.asList(userNotifications);
			global.restrictedSubdomains = (List) Arrays.asList(restrictedSubdomains);
			global.restrictedFieldNames = (List) Arrays.asList(restrictedFieldNames);
			global.restrictedModuleNames = (List) Arrays.asList(restrictedModuleNames);

			global.daysMap = new HashMap<Integer, String>();
			global.daysMap.put(0, "Sun");
			global.daysMap.put(1, "Mon");
			global.daysMap.put(2, "Tue");
			global.daysMap.put(3, "Wed");
			global.daysMap.put(4, "Thu");
			global.daysMap.put(5, "Fri");
			global.daysMap.put(6, "Sat");

			global.defaultModules = (List) Arrays.asList(defaultModules);
			global.languages = (List) Arrays.asList(supportedLanguages);
			global.slaOperators = (List) Arrays.asList(slaOperators);
			global.slaViolationOperators = (List) Arrays.asList(slaViolationOperators);

			// VALID OPERATORS FOR REPORTS
			global.validReportingOperators = new HashMap<String, List<String>>();
			global.validReportingOperators.put("EQUALS_TO",
					Arrays.asList("Text", "Number", "RelationBoolean", "RelationString", "PickListArray", "List Text",
							"Checkbox", "Auto Number", "Chronometer", "NonRelationString", "Formula"));
			global.validReportingOperators.put("NOT_EQUALS_TO",
					Arrays.asList("Text", "Number", "RelationBoolean", "RelationString", "PickListArray", "List Text",
							"Checkbox", "Chronometer", "NonRelationString", "Auto Number", "Formula"));
			global.validReportingOperators.put("REGEX",
					Arrays.asList("Text", "RelationArray", "PickListArray", "List Text"));
			global.validReportingOperators.put("DOES_NOT_CONTAIN", Arrays.asList("Text", "RelationArray",
					"RelationString", "PickListArray", "List Text", "NonRelationString"));
			global.validReportingOperators.put("CONTAINS", Arrays.asList("Text", "RelationArray", "RelationString",
					"PickListArray", "List Text", "NonRelationString"));
			global.validReportingOperators.put("GREATER_THAN",
					Arrays.asList("Number", "Date", "Time", "Date/Time", "Chronometer", "Auto Number", "Formula"));
			global.validReportingOperators.put("LESS_THAN",
					Arrays.asList("Number", "Date", "Time", "Date/Time", "Chronometer", "Auto Number", "Formula"));
			global.validReportingOperators.put("DAYS_BEFORE_TODAY", Arrays.asList("Date", "Date/Time"));

			global.validTextOperators = (List) Arrays.asList(validTextOperators);
			global.validNumericOperators = (List) Arrays.asList(validNumericOperators);
			global.validDateOperators = (List) Arrays.asList(validDateOperators);
			global.validRelationOperators = (List) Arrays.asList(validRelationOperators);
			global.deletionFeedback = (List) Arrays.asList(deletionFeedback);

			global.validChatPromptOperators = (List) Arrays.asList(chatPromptOperators);
			global.validChatPromptConditions = (List) Arrays.asList(chatPromptConditions);
			global.validChatPromptTriggers = (List) Arrays.asList(chatPromptTriggers);

			global.validInstallerPlatforms = (List) Arrays.asList(validInstallerPlatforms);

//			// CONNECT TO THE WEBSOCKET AND POST THE DISCUSSION MESSAGE
//			String url = "ws://" + env.getProperty("manager.host") + ":9081/ngdesk/ngdesk-websocket";
//
//			log.debug("Testing Connection");
//
//			log.debug("Url: " + url);
//
//			ListenableFuture<StompSession> managerWebSocketSession = new ManagerWebSocket().connect(url);
//			
//			Global.stompSession = managerWebSocketSession.get();
//			log.debug("Connection: " + Global.stompSession);

			String countriesFile = global.getFile("countries.json");
			JSONObject countriesJson = new JSONObject(countriesFile);
			JSONArray countries = countriesJson.getJSONArray("COUNTRIES");

			Global.countriesMap = new HashMap<String, String>();
			for (int i = 0; i < countries.length(); i++) {
				JSONObject country = countries.getJSONObject(i);
				Global.countriesMap.put(country.getString("name"), country.getString("code"));
			}

			// adding translations
			global.errors.put("ar", new JSONObject(global.getFile("ar.json")));
			global.errors.put("de", new JSONObject(global.getFile("de.json")));
			global.errors.put("el", new JSONObject(global.getFile("el.json")));
			global.errors.put("en", new JSONObject(global.getFile("en.json")));
			global.errors.put("es", new JSONObject(global.getFile("es.json")));
			global.errors.put("fr", new JSONObject(global.getFile("fr.json")));
			global.errors.put("hi", new JSONObject(global.getFile("hi.json")));
			global.errors.put("it", new JSONObject(global.getFile("it.json")));
			global.errors.put("ms", new JSONObject(global.getFile("ms.json")));
			global.errors.put("no", new JSONObject(global.getFile("no.json")));
			global.errors.put("pt", new JSONObject(global.getFile("pt.json")));
			global.errors.put("ru", new JSONObject(global.getFile("ru.json")));
			global.errors.put("zh", new JSONObject(global.getFile("zh.json")));

		} catch (Exception e) {
			e.printStackTrace();
		}

		log.trace("Exit Startup");
	}

}
