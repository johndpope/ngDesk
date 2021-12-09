import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

export interface Country {
	COUNTRY_NAME: string;
	COUNTRY_CODE: string;
	COUNTRY_DIAL_CODE: string;
	COUNTRY_FLAG: string;
}

@Injectable({
	providedIn: 'root',
})
export class RenderLayoutService {
	constructor() {}

	private adminSignup = new Subject<any>();
	private listLayoutData;

	public countries: Country[] = [
		{
			COUNTRY_NAME: 'Afghanistan (‫افغانستان‬‎)',
			COUNTRY_CODE: 'af',
			COUNTRY_DIAL_CODE: '+93',
			COUNTRY_FLAG: 'af.svg',
		},
		{
			COUNTRY_NAME: 'Albania (Shqipëri)',
			COUNTRY_CODE: 'al',
			COUNTRY_DIAL_CODE: '+355',
			COUNTRY_FLAG: 'al.svg',
		},
		{
			COUNTRY_NAME: 'Algeria (‫الجزائر‬‎)',
			COUNTRY_CODE: 'dz',
			COUNTRY_DIAL_CODE: '+213',
			COUNTRY_FLAG: 'dz.svg',
		},
		{
			COUNTRY_NAME: 'American Samoa',
			COUNTRY_CODE: 'as',
			COUNTRY_DIAL_CODE: '+1684',
			COUNTRY_FLAG: 'as.svg',
		},
		{
			COUNTRY_NAME: 'Andorra',
			COUNTRY_CODE: 'ad',
			COUNTRY_DIAL_CODE: '+376',
			COUNTRY_FLAG: 'ad.svg',
		},
		{
			COUNTRY_NAME: 'Angola',
			COUNTRY_CODE: 'ao',
			COUNTRY_DIAL_CODE: '+244',
			COUNTRY_FLAG: 'ao.svg',
		},
		{
			COUNTRY_NAME: 'Anguilla',
			COUNTRY_CODE: 'ai',
			COUNTRY_DIAL_CODE: '+1264',
			COUNTRY_FLAG: 'ai.svg',
		},
		{
			COUNTRY_NAME: 'Antigua and Barbuda',
			COUNTRY_CODE: 'ag',
			COUNTRY_DIAL_CODE: '+1268',
			COUNTRY_FLAG: 'ag.svg',
		},
		{
			COUNTRY_NAME: 'Argentina',
			COUNTRY_CODE: 'ar',
			COUNTRY_DIAL_CODE: '+54',
			COUNTRY_FLAG: 'ar.svg',
		},
		{
			COUNTRY_NAME: 'Armenia (Հայաստան)',
			COUNTRY_CODE: 'am',
			COUNTRY_DIAL_CODE: '+374',
			COUNTRY_FLAG: 'am.svg',
		},
		{
			COUNTRY_NAME: 'Aruba',
			COUNTRY_CODE: 'aw',
			COUNTRY_DIAL_CODE: '+297',
			COUNTRY_FLAG: 'aw.svg',
		},
		{
			COUNTRY_NAME: 'Australia',
			COUNTRY_CODE: 'au',
			COUNTRY_DIAL_CODE: '+61',
			COUNTRY_FLAG: 'au.svg',
		},
		{
			COUNTRY_NAME: 'Austria (Österreich)',
			COUNTRY_CODE: 'at',
			COUNTRY_DIAL_CODE: '+43',
			COUNTRY_FLAG: 'at.svg',
		},
		{
			COUNTRY_NAME: 'Azerbaijan (Azərbaycan)',
			COUNTRY_CODE: 'az',
			COUNTRY_DIAL_CODE: '+994',
			COUNTRY_FLAG: 'az.svg',
		},
		{
			COUNTRY_NAME: 'Bahamas',
			COUNTRY_CODE: 'bs',
			COUNTRY_DIAL_CODE: '+1242',
			COUNTRY_FLAG: 'bs.svg',
		},
		{
			COUNTRY_NAME: 'Bahrain (‫البحرين‬‎)',
			COUNTRY_CODE: 'bh',
			COUNTRY_DIAL_CODE: '+973',
			COUNTRY_FLAG: 'bh.svg',
		},
		{
			COUNTRY_NAME: 'Bangladesh (বাংলাদেশ)',
			COUNTRY_CODE: 'bd',
			COUNTRY_DIAL_CODE: '+880',
			COUNTRY_FLAG: 'bd.svg',
		},
		{
			COUNTRY_NAME: 'Barbados',
			COUNTRY_CODE: 'bb',
			COUNTRY_DIAL_CODE: '+1246',
			COUNTRY_FLAG: 'bb.svg',
		},
		{
			COUNTRY_NAME: 'Belarus (Беларусь)',
			COUNTRY_CODE: 'by',
			COUNTRY_DIAL_CODE: '+375',
			COUNTRY_FLAG: 'by.svg',
		},
		{
			COUNTRY_NAME: 'Belgium (België)',
			COUNTRY_CODE: 'be',
			COUNTRY_DIAL_CODE: '+32',
			COUNTRY_FLAG: 'be.svg',
		},
		{
			COUNTRY_NAME: 'Belize',
			COUNTRY_CODE: 'bz',
			COUNTRY_DIAL_CODE: '+501',
			COUNTRY_FLAG: 'bz.svg',
		},
		{
			COUNTRY_NAME: 'Benin (Bénin)',
			COUNTRY_CODE: 'bj',
			COUNTRY_DIAL_CODE: '+229',
			COUNTRY_FLAG: 'bj.svg',
		},
		{
			COUNTRY_NAME: 'Bermuda',
			COUNTRY_CODE: 'bm',
			COUNTRY_DIAL_CODE: '+1441',
			COUNTRY_FLAG: 'bm.svg',
		},
		{
			COUNTRY_NAME: 'Bhutan (འབྲུག)',
			COUNTRY_CODE: 'bt',
			COUNTRY_DIAL_CODE: '+975',
			COUNTRY_FLAG: 'bt.svg',
		},
		{
			COUNTRY_NAME: 'Bolivia',
			COUNTRY_CODE: 'bo',
			COUNTRY_DIAL_CODE: '+591',
			COUNTRY_FLAG: 'bo.svg',
		},
		{
			COUNTRY_NAME: 'Bosnia and Herzegovina (Босна и Херцеговина)',
			COUNTRY_CODE: 'ba',
			COUNTRY_DIAL_CODE: '+387',
			COUNTRY_FLAG: 'ba.svg',
		},
		{
			COUNTRY_NAME: 'Botswana',
			COUNTRY_CODE: 'bw',
			COUNTRY_DIAL_CODE: '+267',
			COUNTRY_FLAG: 'bw.svg',
		},
		{
			COUNTRY_NAME: 'Brazil (Brasil)',
			COUNTRY_CODE: 'br',
			COUNTRY_DIAL_CODE: '+55',
			COUNTRY_FLAG: 'br.svg',
		},
		{
			COUNTRY_NAME: 'British Indian Ocean Territory',
			COUNTRY_CODE: 'io',
			COUNTRY_DIAL_CODE: '+246',
			COUNTRY_FLAG: 'io.svg',
		},
		{
			COUNTRY_NAME: 'British Virgin Islands',
			COUNTRY_CODE: 'vg',
			COUNTRY_DIAL_CODE: '+1284',
			COUNTRY_FLAG: 'vg.svg',
		},
		{
			COUNTRY_NAME: 'Brunei',
			COUNTRY_CODE: 'bn',
			COUNTRY_DIAL_CODE: '+673',
			COUNTRY_FLAG: 'bn.svg',
		},
		{
			COUNTRY_NAME: 'Bulgaria (България)',
			COUNTRY_CODE: 'bg',
			COUNTRY_DIAL_CODE: '+359',
			COUNTRY_FLAG: 'bg.svg',
		},
		{
			COUNTRY_NAME: 'Burkina Faso',
			COUNTRY_CODE: 'bf',
			COUNTRY_DIAL_CODE: '+226',
			COUNTRY_FLAG: 'bf.svg',
		},
		{
			COUNTRY_NAME: 'Burundi (Uburundi)',
			COUNTRY_CODE: 'bi',
			COUNTRY_DIAL_CODE: '+257',
			COUNTRY_FLAG: 'bi.svg',
		},
		{
			COUNTRY_NAME: 'Cambodia (កម្ពុជា)',
			COUNTRY_CODE: 'kh',
			COUNTRY_DIAL_CODE: '+855',
			COUNTRY_FLAG: 'kh.svg',
		},
		{
			COUNTRY_NAME: 'Cameroon (Cameroun)',
			COUNTRY_CODE: 'cm',
			COUNTRY_DIAL_CODE: '+237',
			COUNTRY_FLAG: 'cm.svg',
		},
		{
			COUNTRY_NAME: 'Canada',
			COUNTRY_CODE: 'ca',
			COUNTRY_DIAL_CODE: '+1',
			COUNTRY_FLAG: 'ca.svg',
		},
		{
			COUNTRY_NAME: 'Cape Verde (Kabu Verdi)',
			COUNTRY_CODE: 'cv',
			COUNTRY_DIAL_CODE: '+238',
			COUNTRY_FLAG: 'cv.svg',
		},
		{
			COUNTRY_NAME: 'Caribbean Netherlands',
			COUNTRY_CODE: 'bq',
			COUNTRY_DIAL_CODE: '+599',
			COUNTRY_FLAG: 'bq.svg',
		},
		{
			COUNTRY_NAME: 'Cayman Islands',
			COUNTRY_CODE: 'ky',
			COUNTRY_DIAL_CODE: '+1345',
			COUNTRY_FLAG: 'ky.svg',
		},
		{
			COUNTRY_NAME: 'Central African Republic (République centrafricaine)',
			COUNTRY_CODE: 'cf',
			COUNTRY_DIAL_CODE: '+236',
			COUNTRY_FLAG: 'cf.svg',
		},
		{
			COUNTRY_NAME: 'Chad (Tchad)',
			COUNTRY_CODE: 'td',
			COUNTRY_DIAL_CODE: '+235',
			COUNTRY_FLAG: 'td.svg',
		},
		{
			COUNTRY_NAME: 'Chile',
			COUNTRY_CODE: 'cl',
			COUNTRY_DIAL_CODE: '+56',
			COUNTRY_FLAG: 'cl.svg',
		},
		{
			COUNTRY_NAME: 'China (中国)',
			COUNTRY_CODE: 'cn',
			COUNTRY_DIAL_CODE: '+86',
			COUNTRY_FLAG: 'cn.svg',
		},
		{
			COUNTRY_NAME: 'Christmas Island',
			COUNTRY_CODE: 'cx',
			COUNTRY_DIAL_CODE: '+61',
			COUNTRY_FLAG: 'cx.svg',
		},
		{
			COUNTRY_NAME: 'Cocos (Keeling) Islands',
			COUNTRY_CODE: 'cc',
			COUNTRY_DIAL_CODE: '+61',
			COUNTRY_FLAG: 'cc.svg',
		},
		{
			COUNTRY_NAME: 'Colombia',
			COUNTRY_CODE: 'co',
			COUNTRY_DIAL_CODE: '+57',
			COUNTRY_FLAG: 'co.svg',
		},
		{
			COUNTRY_NAME: 'Comoros (‫جزر القمر‬‎)',
			COUNTRY_CODE: 'km',
			COUNTRY_DIAL_CODE: '+269',
			COUNTRY_FLAG: 'km.svg',
		},
		{
			COUNTRY_NAME: 'Congo (DRC) (Jamhuri ya Kidemokrasia ya Kongo)',
			COUNTRY_CODE: 'cd',
			COUNTRY_DIAL_CODE: '+243',
			COUNTRY_FLAG: 'cd.svg',
		},
		{
			COUNTRY_NAME: 'Congo (Republic) (Congo-Brazzaville)',
			COUNTRY_CODE: 'cg',
			COUNTRY_DIAL_CODE: '+242',
			COUNTRY_FLAG: 'cg.svg',
		},
		{
			COUNTRY_NAME: 'Cook Islands',
			COUNTRY_CODE: 'ck',
			COUNTRY_DIAL_CODE: '+682',
			COUNTRY_FLAG: 'ck.svg',
		},
		{
			COUNTRY_NAME: 'Costa Rica',
			COUNTRY_CODE: 'cr',
			COUNTRY_DIAL_CODE: '+506',
			COUNTRY_FLAG: 'cr.svg',
		},
		{
			COUNTRY_NAME: 'Côte d’Ivoire',
			COUNTRY_CODE: 'ci',
			COUNTRY_DIAL_CODE: '+225',
			COUNTRY_FLAG: 'ci.svg',
		},
		{
			COUNTRY_NAME: 'Croatia (Hrvatska)',
			COUNTRY_CODE: 'hr',
			COUNTRY_DIAL_CODE: '+385',
			COUNTRY_FLAG: 'hr.svg',
		},
		{
			COUNTRY_NAME: 'Cuba',
			COUNTRY_CODE: 'cu',
			COUNTRY_DIAL_CODE: '+53',
			COUNTRY_FLAG: 'cu.svg',
		},
		{
			COUNTRY_NAME: 'Curaçao',
			COUNTRY_CODE: 'cw',
			COUNTRY_DIAL_CODE: '+599',
			COUNTRY_FLAG: 'cw.svg',
		},
		{
			COUNTRY_NAME: 'Cyprus (Κύπρος)',
			COUNTRY_CODE: 'cy',
			COUNTRY_DIAL_CODE: '+357',
			COUNTRY_FLAG: 'cy.svg',
		},
		{
			COUNTRY_NAME: 'Czech Republic (Česká republika)',
			COUNTRY_CODE: 'cz',
			COUNTRY_DIAL_CODE: '+420',
			COUNTRY_FLAG: 'cz.svg',
		},
		{
			COUNTRY_NAME: 'Denmark (Danmark)',
			COUNTRY_CODE: 'dk',
			COUNTRY_DIAL_CODE: '+45',
			COUNTRY_FLAG: 'dk.svg',
		},
		{
			COUNTRY_NAME: 'Djibouti',
			COUNTRY_CODE: 'dj',
			COUNTRY_DIAL_CODE: '+253',
			COUNTRY_FLAG: 'dj.svg',
		},
		{
			COUNTRY_NAME: 'Dominica',
			COUNTRY_CODE: 'dm',
			COUNTRY_DIAL_CODE: '+1767',
			COUNTRY_FLAG: 'dm.svg',
		},
		{
			COUNTRY_NAME: 'Dominican Republic (República Dominicana)',
			COUNTRY_CODE: 'do',
			COUNTRY_DIAL_CODE: '+1',
			COUNTRY_FLAG: 'do.svg',
		},
		{
			COUNTRY_NAME: 'Ecuador',
			COUNTRY_CODE: 'ec',
			COUNTRY_DIAL_CODE: '+593',
			COUNTRY_FLAG: 'ec.svg',
		},
		{
			COUNTRY_NAME: 'Egypt (‫مصر‬‎)',
			COUNTRY_CODE: 'eg',
			COUNTRY_DIAL_CODE: '+20',
			COUNTRY_FLAG: 'eg.svg',
		},
		{
			COUNTRY_NAME: 'El Salvador',
			COUNTRY_CODE: 'sv',
			COUNTRY_DIAL_CODE: '+503',
			COUNTRY_FLAG: 'sv.svg',
		},
		{
			COUNTRY_NAME: 'Equatorial Guinea (Guinea Ecuatorial)',
			COUNTRY_CODE: 'gq',
			COUNTRY_DIAL_CODE: '+240',
			COUNTRY_FLAG: 'gq.svg',
		},
		{
			COUNTRY_NAME: 'Eritrea',
			COUNTRY_CODE: 'er',
			COUNTRY_DIAL_CODE: '+291',
			COUNTRY_FLAG: 'er.svg',
		},
		{
			COUNTRY_NAME: 'Estonia (Eesti)',
			COUNTRY_CODE: 'ee',
			COUNTRY_DIAL_CODE: '+372',
			COUNTRY_FLAG: 'ee.svg',
		},
		{
			COUNTRY_NAME: 'Ethiopia',
			COUNTRY_CODE: 'et',
			COUNTRY_DIAL_CODE: '+251',
			COUNTRY_FLAG: 'et.svg',
		},
		{
			COUNTRY_NAME: 'Falkland Islands (Islas Malvinas)',
			COUNTRY_CODE: 'fk',
			COUNTRY_DIAL_CODE: '+500',
			COUNTRY_FLAG: 'fk.svg',
		},
		{
			COUNTRY_NAME: 'Faroe Islands (Føroyar)',
			COUNTRY_CODE: 'fo',
			COUNTRY_DIAL_CODE: '+298',
			COUNTRY_FLAG: 'fo.svg',
		},
		{
			COUNTRY_NAME: 'Fiji',
			COUNTRY_CODE: 'fj',
			COUNTRY_DIAL_CODE: '+679',
			COUNTRY_FLAG: 'fj.svg',
		},
		{
			COUNTRY_NAME: 'Finland (Suomi)',
			COUNTRY_CODE: 'fi',
			COUNTRY_DIAL_CODE: '+358',
			COUNTRY_FLAG: 'fi.svg',
		},
		{
			COUNTRY_NAME: 'France',
			COUNTRY_CODE: 'fr',
			COUNTRY_DIAL_CODE: '+33',
			COUNTRY_FLAG: 'fr.svg',
		},
		{
			COUNTRY_NAME: 'French Guiana (Guyane française)',
			COUNTRY_CODE: 'gf',
			COUNTRY_DIAL_CODE: '+594',
			COUNTRY_FLAG: 'gf.svg',
		},
		{
			COUNTRY_NAME: 'French Polynesia (Polynésie française)',
			COUNTRY_CODE: 'pf',
			COUNTRY_DIAL_CODE: '+689',
			COUNTRY_FLAG: 'pf.svg',
		},
		{
			COUNTRY_NAME: 'Gabon',
			COUNTRY_CODE: 'ga',
			COUNTRY_DIAL_CODE: '+241',
			COUNTRY_FLAG: 'ga.svg',
		},
		{
			COUNTRY_NAME: 'Gambia',
			COUNTRY_CODE: 'gm',
			COUNTRY_DIAL_CODE: '+220',
			COUNTRY_FLAG: 'gm.svg',
		},
		{
			COUNTRY_NAME: 'Georgia (საქართველო)',
			COUNTRY_CODE: 'ge',
			COUNTRY_DIAL_CODE: '+995',
			COUNTRY_FLAG: 'ge.svg',
		},
		{
			COUNTRY_NAME: 'Germany (Deutschland)',
			COUNTRY_CODE: 'de',
			COUNTRY_DIAL_CODE: '+49',
			COUNTRY_FLAG: 'de.svg',
		},
		{
			COUNTRY_NAME: 'Ghana (Gaana)',
			COUNTRY_CODE: 'gh',
			COUNTRY_DIAL_CODE: '+233',
			COUNTRY_FLAG: 'gh.svg',
		},
		{
			COUNTRY_NAME: 'Gibraltar',
			COUNTRY_CODE: 'gi',
			COUNTRY_DIAL_CODE: '+350',
			COUNTRY_FLAG: 'gi.svg',
		},
		{
			COUNTRY_NAME: 'Greece (Ελλάδα)',
			COUNTRY_CODE: 'gr',
			COUNTRY_DIAL_CODE: '+30',
			COUNTRY_FLAG: 'gr.svg',
		},
		{
			COUNTRY_NAME: 'Greenland (Kalaallit Nunaat)',
			COUNTRY_CODE: 'gl',
			COUNTRY_DIAL_CODE: '+299',
			COUNTRY_FLAG: 'gl.svg',
		},
		{
			COUNTRY_NAME: 'Grenada',
			COUNTRY_CODE: 'gd',
			COUNTRY_DIAL_CODE: '+1473',
			COUNTRY_FLAG: 'gd.svg',
		},
		{
			COUNTRY_NAME: 'Guadeloupe',
			COUNTRY_CODE: 'gp',
			COUNTRY_DIAL_CODE: '+590',
			COUNTRY_FLAG: 'gp.svg',
		},
		{
			COUNTRY_NAME: 'Guam',
			COUNTRY_CODE: 'gu',
			COUNTRY_DIAL_CODE: '+1671',
			COUNTRY_FLAG: 'gu.svg',
		},
		{
			COUNTRY_NAME: 'Guatemala',
			COUNTRY_CODE: 'gt',
			COUNTRY_DIAL_CODE: '+502',
			COUNTRY_FLAG: 'gt.svg',
		},
		{
			COUNTRY_NAME: 'Guernsey',
			COUNTRY_CODE: 'gg',
			COUNTRY_DIAL_CODE: '+44',
			COUNTRY_FLAG: 'gg.svg',
		},
		{
			COUNTRY_NAME: 'Guinea (Guinée)',
			COUNTRY_CODE: 'gn',
			COUNTRY_DIAL_CODE: '+224',
			COUNTRY_FLAG: 'gn.svg',
		},
		{
			COUNTRY_NAME: 'Guinea-Bissau (Guiné Bissau)',
			COUNTRY_CODE: 'gw',
			COUNTRY_DIAL_CODE: '+245',
			COUNTRY_FLAG: 'gw.svg',
		},
		{
			COUNTRY_NAME: 'Guyana',
			COUNTRY_CODE: 'gy',
			COUNTRY_DIAL_CODE: '+592',
			COUNTRY_FLAG: 'gy.svg',
		},
		{
			COUNTRY_NAME: 'Haiti',
			COUNTRY_CODE: 'ht',
			COUNTRY_DIAL_CODE: '+509',
			COUNTRY_FLAG: 'ht.svg',
		},
		{
			COUNTRY_NAME: 'Honduras',
			COUNTRY_CODE: 'hn',
			COUNTRY_DIAL_CODE: '+504',
			COUNTRY_FLAG: 'hn.svg',
		},
		{
			COUNTRY_NAME: 'Hong Kong (香港)',
			COUNTRY_CODE: 'hk',
			COUNTRY_DIAL_CODE: '+852',
			COUNTRY_FLAG: 'hk.svg',
		},
		{
			COUNTRY_NAME: 'Hungary (Magyarország)',
			COUNTRY_CODE: 'hu',
			COUNTRY_DIAL_CODE: '+36',
			COUNTRY_FLAG: 'hu.svg',
		},
		{
			COUNTRY_NAME: 'Iceland (Ísland)',
			COUNTRY_CODE: 'is',
			COUNTRY_DIAL_CODE: '+354',
			COUNTRY_FLAG: 'is.svg',
		},
		{
			COUNTRY_NAME: 'India (भारत)',
			COUNTRY_CODE: 'in',
			COUNTRY_DIAL_CODE: '+91',
			COUNTRY_FLAG: 'in.svg',
		},
		{
			COUNTRY_NAME: 'Indonesia',
			COUNTRY_CODE: 'id',
			COUNTRY_DIAL_CODE: '+62',
			COUNTRY_FLAG: 'id.svg',
		},
		{
			COUNTRY_NAME: 'Iran (‫ایران‬‎)',
			COUNTRY_CODE: 'ir',
			COUNTRY_DIAL_CODE: '+98',
			COUNTRY_FLAG: 'ir.svg',
		},
		{
			COUNTRY_NAME: 'Iraq (‫العراق‬‎)',
			COUNTRY_CODE: 'iq',
			COUNTRY_DIAL_CODE: '+964',
			COUNTRY_FLAG: 'iq.svg',
		},
		{
			COUNTRY_NAME: 'Ireland',
			COUNTRY_CODE: 'ie',
			COUNTRY_DIAL_CODE: '+353',
			COUNTRY_FLAG: 'ie.svg',
		},
		{
			COUNTRY_NAME: 'Isle of Man',
			COUNTRY_CODE: 'im',
			COUNTRY_DIAL_CODE: '+44',
			COUNTRY_FLAG: 'im.svg',
		},
		{
			COUNTRY_NAME: 'Israel (‫ישראל‬‎)',
			COUNTRY_CODE: 'il',
			COUNTRY_DIAL_CODE: '+972',
			COUNTRY_FLAG: 'il.svg',
		},
		{
			COUNTRY_NAME: 'Italy (Italia)',
			COUNTRY_CODE: 'it',
			COUNTRY_DIAL_CODE: '+39',
			COUNTRY_FLAG: 'it.svg',
		},
		{
			COUNTRY_NAME: 'Jamaica',
			COUNTRY_CODE: 'jm',
			COUNTRY_DIAL_CODE: '+1',
			COUNTRY_FLAG: 'jm.svg',
		},
		{
			COUNTRY_NAME: 'Japan (日本)',
			COUNTRY_CODE: 'jp',
			COUNTRY_DIAL_CODE: '+81',
			COUNTRY_FLAG: 'jp.svg',
		},
		{
			COUNTRY_NAME: 'Jersey',
			COUNTRY_CODE: 'je',
			COUNTRY_DIAL_CODE: '+44',
			COUNTRY_FLAG: 'je.svg',
		},
		{
			COUNTRY_NAME: 'Jordan (‫الأردن‬‎)',
			COUNTRY_CODE: 'jo',
			COUNTRY_DIAL_CODE: '+962',
			COUNTRY_FLAG: 'jo.svg',
		},
		{
			COUNTRY_NAME: 'Kazakhstan (Казахстан)',
			COUNTRY_CODE: 'kz',
			COUNTRY_DIAL_CODE: '+7',
			COUNTRY_FLAG: 'kz.svg',
		},
		{
			COUNTRY_NAME: 'Kenya',
			COUNTRY_CODE: 'ke',
			COUNTRY_DIAL_CODE: '+254',
			COUNTRY_FLAG: 'ke.svg',
		},
		{
			COUNTRY_NAME: 'Kiribati',
			COUNTRY_CODE: 'ki',
			COUNTRY_DIAL_CODE: '+686',
			COUNTRY_FLAG: 'ki.svg',
		},
		{
			COUNTRY_NAME: 'Kosovo',
			COUNTRY_CODE: 'xk',
			COUNTRY_DIAL_CODE: '+383',
			COUNTRY_FLAG: 'xk.svg',
		},
		{
			COUNTRY_NAME: 'Kuwait (‫الكويت‬‎)',
			COUNTRY_CODE: 'kw',
			COUNTRY_DIAL_CODE: '+965',
			COUNTRY_FLAG: 'kw.svg',
		},
		{
			COUNTRY_NAME: 'Kyrgyzstan (Кыргызстан)',
			COUNTRY_CODE: 'kg',
			COUNTRY_DIAL_CODE: '+996',
			COUNTRY_FLAG: 'kg.svg',
		},
		{
			COUNTRY_NAME: 'Laos (ລາວ)',
			COUNTRY_CODE: 'la',
			COUNTRY_DIAL_CODE: '+856',
			COUNTRY_FLAG: 'la.svg',
		},
		{
			COUNTRY_NAME: 'Latvia (Latvija)',
			COUNTRY_CODE: 'lv',
			COUNTRY_DIAL_CODE: '+371',
			COUNTRY_FLAG: 'lv.svg',
		},
		{
			COUNTRY_NAME: 'Lebanon (‫لبنان‬‎)',
			COUNTRY_CODE: 'lb',
			COUNTRY_DIAL_CODE: '+961',
			COUNTRY_FLAG: 'lb.svg',
		},
		{
			COUNTRY_NAME: 'Lesotho',
			COUNTRY_CODE: 'ls',
			COUNTRY_DIAL_CODE: '+266',
			COUNTRY_FLAG: 'ls.svg',
		},
		{
			COUNTRY_NAME: 'Liberia',
			COUNTRY_CODE: 'lr',
			COUNTRY_DIAL_CODE: '+231',
			COUNTRY_FLAG: 'lr.svg',
		},
		{
			COUNTRY_NAME: 'Libya (‫ليبيا‬‎)',
			COUNTRY_CODE: 'ly',
			COUNTRY_DIAL_CODE: '+218',
			COUNTRY_FLAG: 'ly.svg',
		},
		{
			COUNTRY_NAME: 'Liechtenstein',
			COUNTRY_CODE: 'li',
			COUNTRY_DIAL_CODE: '+423',
			COUNTRY_FLAG: 'li.svg',
		},
		{
			COUNTRY_NAME: 'Lithuania (Lietuva)',
			COUNTRY_CODE: 'lt',
			COUNTRY_DIAL_CODE: '+370',
			COUNTRY_FLAG: 'lt.svg',
		},
		{
			COUNTRY_NAME: 'Luxembourg',
			COUNTRY_CODE: 'lu',
			COUNTRY_DIAL_CODE: '+352',
			COUNTRY_FLAG: 'lu.svg',
		},
		{
			COUNTRY_NAME: 'Macau (澳門)',
			COUNTRY_CODE: 'mo',
			COUNTRY_DIAL_CODE: '+853',
			COUNTRY_FLAG: 'mo.svg',
		},
		{
			COUNTRY_NAME: 'Macedonia (FYROM) (Македонија)',
			COUNTRY_CODE: 'mk',
			COUNTRY_DIAL_CODE: '+389',
			COUNTRY_FLAG: 'mk.svg',
		},
		{
			COUNTRY_NAME: 'Madagascar (Madagasikara)',
			COUNTRY_CODE: 'mg',
			COUNTRY_DIAL_CODE: '+261',
			COUNTRY_FLAG: 'mg.svg',
		},
		{
			COUNTRY_NAME: 'Malawi',
			COUNTRY_CODE: 'mw',
			COUNTRY_DIAL_CODE: '+265',
			COUNTRY_FLAG: 'mw.svg',
		},
		{
			COUNTRY_NAME: 'Malaysia',
			COUNTRY_CODE: 'my',
			COUNTRY_DIAL_CODE: '+60',
			COUNTRY_FLAG: 'my.svg',
		},
		{
			COUNTRY_NAME: 'Maldives',
			COUNTRY_CODE: 'mv',
			COUNTRY_DIAL_CODE: '+960',
			COUNTRY_FLAG: 'mv.svg',
		},
		{
			COUNTRY_NAME: 'Mali',
			COUNTRY_CODE: 'ml',
			COUNTRY_DIAL_CODE: '+223',
			COUNTRY_FLAG: 'ml.svg',
		},
		{
			COUNTRY_NAME: 'Malta',
			COUNTRY_CODE: 'mt',
			COUNTRY_DIAL_CODE: '+356',
			COUNTRY_FLAG: 'mt.svg',
		},
		{
			COUNTRY_NAME: 'Marshall Islands',
			COUNTRY_CODE: 'mh',
			COUNTRY_DIAL_CODE: '+692',
			COUNTRY_FLAG: 'mh.svg',
		},
		{
			COUNTRY_NAME: 'Martinique',
			COUNTRY_CODE: 'mq',
			COUNTRY_DIAL_CODE: '+596',
			COUNTRY_FLAG: 'mq.svg',
		},
		{
			COUNTRY_NAME: 'Mauritania (‫موريتانيا‬‎)',
			COUNTRY_CODE: 'mr',
			COUNTRY_DIAL_CODE: '+222',
			COUNTRY_FLAG: 'mr.svg',
		},
		{
			COUNTRY_NAME: 'Mauritius (Moris)',
			COUNTRY_CODE: 'mu',
			COUNTRY_DIAL_CODE: '+230',
			COUNTRY_FLAG: 'mu.svg',
		},
		{
			COUNTRY_NAME: 'Mayotte',
			COUNTRY_CODE: 'yt',
			COUNTRY_DIAL_CODE: '+262',
			COUNTRY_FLAG: 'yt.svg',
		},
		{
			COUNTRY_NAME: 'Mexico (México)',
			COUNTRY_CODE: 'mx',
			COUNTRY_DIAL_CODE: '+52',
			COUNTRY_FLAG: 'mx.svg',
		},
		{
			COUNTRY_NAME: 'Micronesia',
			COUNTRY_CODE: 'fm',
			COUNTRY_DIAL_CODE: '+691',
			COUNTRY_FLAG: 'fm.svg',
		},
		{
			COUNTRY_NAME: 'Moldova (Republica Moldova)',
			COUNTRY_CODE: 'md',
			COUNTRY_DIAL_CODE: '+373',
			COUNTRY_FLAG: 'md.svg',
		},
		{
			COUNTRY_NAME: 'Monaco',
			COUNTRY_CODE: 'mc',
			COUNTRY_DIAL_CODE: '+377',
			COUNTRY_FLAG: 'mc.svg',
		},
		{
			COUNTRY_NAME: 'Mongolia (Монгол)',
			COUNTRY_CODE: 'mn',
			COUNTRY_DIAL_CODE: '+976',
			COUNTRY_FLAG: 'mn.svg',
		},
		{
			COUNTRY_NAME: 'Montenegro (Crna Gora)',
			COUNTRY_CODE: 'me',
			COUNTRY_DIAL_CODE: '+382',
			COUNTRY_FLAG: 'me.svg',
		},
		{
			COUNTRY_NAME: 'Montserrat',
			COUNTRY_CODE: 'ms',
			COUNTRY_DIAL_CODE: '+1664',
			COUNTRY_FLAG: 'ms.svg',
		},
		{
			COUNTRY_NAME: 'Morocco (‫المغرب‬‎)',
			COUNTRY_CODE: 'ma',
			COUNTRY_DIAL_CODE: '+212',
			COUNTRY_FLAG: 'ma.svg',
		},
		{
			COUNTRY_NAME: 'Mozambique (Moçambique)',
			COUNTRY_CODE: 'mz',
			COUNTRY_DIAL_CODE: '+258',
			COUNTRY_FLAG: 'mz.svg',
		},
		{
			COUNTRY_NAME: 'Myanmar (Burma) (မြန်မာ)',
			COUNTRY_CODE: 'mm',
			COUNTRY_DIAL_CODE: '+95',
			COUNTRY_FLAG: 'mm.svg',
		},
		{
			COUNTRY_NAME: 'Namibia (Namibië)',
			COUNTRY_CODE: 'na',
			COUNTRY_DIAL_CODE: '+264',
			COUNTRY_FLAG: 'na.svg',
		},
		{
			COUNTRY_NAME: 'Nauru',
			COUNTRY_CODE: 'nr',
			COUNTRY_DIAL_CODE: '+674',
			COUNTRY_FLAG: 'nr.svg',
		},
		{
			COUNTRY_NAME: 'Nepal (नेपाल)',
			COUNTRY_CODE: 'np',
			COUNTRY_DIAL_CODE: '+977',
			COUNTRY_FLAG: 'np.svg',
		},
		{
			COUNTRY_NAME: 'Netherlands (Nederland)',
			COUNTRY_CODE: 'nl',
			COUNTRY_DIAL_CODE: '+31',
			COUNTRY_FLAG: 'nl.svg',
		},
		{
			COUNTRY_NAME: 'New Caledonia (Nouvelle-Calédonie)',
			COUNTRY_CODE: 'nc',
			COUNTRY_DIAL_CODE: '+687',
			COUNTRY_FLAG: 'nc.svg',
		},
		{
			COUNTRY_NAME: 'New Zealand',
			COUNTRY_CODE: 'nz',
			COUNTRY_DIAL_CODE: '+64',
			COUNTRY_FLAG: 'nz.svg',
		},
		{
			COUNTRY_NAME: 'Nicaragua',
			COUNTRY_CODE: 'ni',
			COUNTRY_DIAL_CODE: '+505',
			COUNTRY_FLAG: 'ni.svg',
		},
		{
			COUNTRY_NAME: 'Niger (Nijar)',
			COUNTRY_CODE: 'ne',
			COUNTRY_DIAL_CODE: '+227',
			COUNTRY_FLAG: 'ne.svg',
		},
		{
			COUNTRY_NAME: 'Nigeria',
			COUNTRY_CODE: 'ng',
			COUNTRY_DIAL_CODE: '+234',
			COUNTRY_FLAG: 'ng.svg',
		},
		{
			COUNTRY_NAME: 'Niue',
			COUNTRY_CODE: 'nu',
			COUNTRY_DIAL_CODE: '+683',
			COUNTRY_FLAG: 'nu.svg',
		},
		{
			COUNTRY_NAME: 'Norfolk Island',
			COUNTRY_CODE: 'nf',
			COUNTRY_DIAL_CODE: '+672',
			COUNTRY_FLAG: 'nf.svg',
		},
		{
			COUNTRY_NAME: 'North Korea (조선 민주주의 인민 공화국)',
			COUNTRY_CODE: 'kp',
			COUNTRY_DIAL_CODE: '+850',
			COUNTRY_FLAG: 'kp.svg',
		},
		{
			COUNTRY_NAME: 'Northern Mariana Islands',
			COUNTRY_CODE: 'mp',
			COUNTRY_DIAL_CODE: '+1670',
			COUNTRY_FLAG: 'mp.svg',
		},
		{
			COUNTRY_NAME: 'Norway (Norge)',
			COUNTRY_CODE: 'no',
			COUNTRY_DIAL_CODE: '+47',
			COUNTRY_FLAG: 'no.svg',
		},
		{
			COUNTRY_NAME: 'Oman (‫عُمان‬‎)',
			COUNTRY_CODE: 'om',
			COUNTRY_DIAL_CODE: '+968',
			COUNTRY_FLAG: 'om.svg',
		},
		{
			COUNTRY_NAME: 'Pakistan (‫پاکستان‬‎)',
			COUNTRY_CODE: 'pk',
			COUNTRY_DIAL_CODE: '+92',
			COUNTRY_FLAG: 'pk.svg',
		},
		{
			COUNTRY_NAME: 'Palau',
			COUNTRY_CODE: 'pw',
			COUNTRY_DIAL_CODE: '+680',
			COUNTRY_FLAG: 'pw.svg',
		},
		{
			COUNTRY_NAME: 'Palestine (‫فلسطين‬‎)',
			COUNTRY_CODE: 'ps',
			COUNTRY_DIAL_CODE: '+970',
			COUNTRY_FLAG: 'ps.svg',
		},
		{
			COUNTRY_NAME: 'Panama (Panamá)',
			COUNTRY_CODE: 'pa',
			COUNTRY_DIAL_CODE: '+507',
			COUNTRY_FLAG: 'pa.svg',
		},
		{
			COUNTRY_NAME: 'Papua New Guinea',
			COUNTRY_CODE: 'pg',
			COUNTRY_DIAL_CODE: '+675',
			COUNTRY_FLAG: 'pg.svg',
		},
		{
			COUNTRY_NAME: 'Paraguay',
			COUNTRY_CODE: 'py',
			COUNTRY_DIAL_CODE: '+595',
			COUNTRY_FLAG: 'py.svg',
		},
		{
			COUNTRY_NAME: 'Peru (Perú)',
			COUNTRY_CODE: 'pe',
			COUNTRY_DIAL_CODE: '+51',
			COUNTRY_FLAG: 'pe.svg',
		},
		{
			COUNTRY_NAME: 'Philippines',
			COUNTRY_CODE: 'ph',
			COUNTRY_DIAL_CODE: '+63',
			COUNTRY_FLAG: 'ph.svg',
		},
		{
			COUNTRY_NAME: 'Poland (Polska)',
			COUNTRY_CODE: 'pl',
			COUNTRY_DIAL_CODE: '+48',
			COUNTRY_FLAG: 'pl.svg',
		},
		{
			COUNTRY_NAME: 'Portugal',
			COUNTRY_CODE: 'pt',
			COUNTRY_DIAL_CODE: '+351',
			COUNTRY_FLAG: 'pt.svg',
		},
		{
			COUNTRY_NAME: 'Puerto Rico',
			COUNTRY_CODE: 'pr',
			COUNTRY_DIAL_CODE: '+1',
			COUNTRY_FLAG: 'pr.svg',
		},
		{
			COUNTRY_NAME: 'Qatar (‫قطر‬‎)',
			COUNTRY_CODE: 'qa',
			COUNTRY_DIAL_CODE: '+974',
			COUNTRY_FLAG: 'qa.svg',
		},
		{
			COUNTRY_NAME: 'Réunion (La Réunion)',
			COUNTRY_CODE: 're',
			COUNTRY_DIAL_CODE: '+262',
			COUNTRY_FLAG: 're.svg',
		},
		{
			COUNTRY_NAME: 'Romania (România)',
			COUNTRY_CODE: 'ro',
			COUNTRY_DIAL_CODE: '+40',
			COUNTRY_FLAG: 'ro.svg',
		},
		{
			COUNTRY_NAME: 'Russia (Россия)',
			COUNTRY_CODE: 'ru',
			COUNTRY_DIAL_CODE: '+7',
			COUNTRY_FLAG: 'ru.svg',
		},
		{
			COUNTRY_NAME: 'Rwanda',
			COUNTRY_CODE: 'rw',
			COUNTRY_DIAL_CODE: '+250',
			COUNTRY_FLAG: 'rw.svg',
		},
		{
			COUNTRY_NAME: 'Saint Barthélemy',
			COUNTRY_CODE: 'bl',
			COUNTRY_DIAL_CODE: '+590',
			COUNTRY_FLAG: 'bl.svg',
		},
		{
			COUNTRY_NAME: 'Saint Helena',
			COUNTRY_CODE: 'sh',
			COUNTRY_DIAL_CODE: '+290',
			COUNTRY_FLAG: 'sh.svg',
		},
		{
			COUNTRY_NAME: 'Saint Kitts and Nevis',
			COUNTRY_CODE: 'kn',
			COUNTRY_DIAL_CODE: '+1869',
			COUNTRY_FLAG: 'kn.svg',
		},
		{
			COUNTRY_NAME: 'Saint Lucia',
			COUNTRY_CODE: 'lc',
			COUNTRY_DIAL_CODE: '+1758',
			COUNTRY_FLAG: 'lc.svg',
		},
		{
			COUNTRY_NAME: 'Saint Martin (Saint-Martin (partie française))',
			COUNTRY_CODE: 'mf',
			COUNTRY_DIAL_CODE: '+590',
			COUNTRY_FLAG: 'mf.svg',
		},
		{
			COUNTRY_NAME: 'Saint Pierre and Miquelon (Saint-Pierre-et-Miquelon)',
			COUNTRY_CODE: 'pm',
			COUNTRY_DIAL_CODE: '+508',
			COUNTRY_FLAG: 'pm.svg',
		},
		{
			COUNTRY_NAME: 'Saint Vincent and the Grenadines',
			COUNTRY_CODE: 'vc',
			COUNTRY_DIAL_CODE: '+1784',
			COUNTRY_FLAG: 'vc.svg',
		},
		{
			COUNTRY_NAME: 'Samoa',
			COUNTRY_CODE: 'ws',
			COUNTRY_DIAL_CODE: '+685',
			COUNTRY_FLAG: 'ws.svg',
		},
		{
			COUNTRY_NAME: 'San Marino',
			COUNTRY_CODE: 'sm',
			COUNTRY_DIAL_CODE: '+378',
			COUNTRY_FLAG: 'sm.svg',
		},
		{
			COUNTRY_NAME: 'São Tomé and Príncipe (São Tomé e Príncipe)',
			COUNTRY_CODE: 'st',
			COUNTRY_DIAL_CODE: '+239',
			COUNTRY_FLAG: 'st.svg',
		},
		{
			COUNTRY_NAME: 'Saudi Arabia (‫المملكة العربية السعودية‬‎)',
			COUNTRY_CODE: 'sa',
			COUNTRY_DIAL_CODE: '+966',
			COUNTRY_FLAG: 'sa.svg',
		},
		{
			COUNTRY_NAME: 'Senegal (Sénégal)',
			COUNTRY_CODE: 'sn',
			COUNTRY_DIAL_CODE: '+221',
			COUNTRY_FLAG: 'sn.svg',
		},
		{
			COUNTRY_NAME: 'Serbia (Србија)',
			COUNTRY_CODE: 'rs',
			COUNTRY_DIAL_CODE: '+381',
			COUNTRY_FLAG: 'rs.svg',
		},
		{
			COUNTRY_NAME: 'Seychelles',
			COUNTRY_CODE: 'sc',
			COUNTRY_DIAL_CODE: '+248',
			COUNTRY_FLAG: 'sc.svg',
		},
		{
			COUNTRY_NAME: 'Sierra Leone',
			COUNTRY_CODE: 'sl',
			COUNTRY_DIAL_CODE: '+232',
			COUNTRY_FLAG: 'sl.svg',
		},
		{
			COUNTRY_NAME: 'Singapore',
			COUNTRY_CODE: 'sg',
			COUNTRY_DIAL_CODE: '+65',
			COUNTRY_FLAG: 'sg.svg',
		},
		{
			COUNTRY_NAME: 'Sint Maarten',
			COUNTRY_CODE: 'sx',
			COUNTRY_DIAL_CODE: '+1721',
			COUNTRY_FLAG: 'sx.svg',
		},
		{
			COUNTRY_NAME: 'Slovakia (Slovensko)',
			COUNTRY_CODE: 'sk',
			COUNTRY_DIAL_CODE: '+421',
			COUNTRY_FLAG: 'sk.svg',
		},
		{
			COUNTRY_NAME: 'Slovenia (Slovenija)',
			COUNTRY_CODE: 'si',
			COUNTRY_DIAL_CODE: '+386',
			COUNTRY_FLAG: 'si.svg',
		},
		{
			COUNTRY_NAME: 'Solomon Islands',
			COUNTRY_CODE: 'sb',
			COUNTRY_DIAL_CODE: '+677',
			COUNTRY_FLAG: 'sb.svg',
		},
		{
			COUNTRY_NAME: 'Somalia (Soomaaliya)',
			COUNTRY_CODE: 'so',
			COUNTRY_DIAL_CODE: '+252',
			COUNTRY_FLAG: 'so.svg',
		},
		{
			COUNTRY_NAME: 'South Africa',
			COUNTRY_CODE: 'za',
			COUNTRY_DIAL_CODE: '+27',
			COUNTRY_FLAG: 'za.svg',
		},
		{
			COUNTRY_NAME: 'South Korea (대한민국)',
			COUNTRY_CODE: 'kr',
			COUNTRY_DIAL_CODE: '+82',
			COUNTRY_FLAG: 'kr.svg',
		},
		{
			COUNTRY_NAME: 'South Sudan (‫جنوب السودان‬‎)',
			COUNTRY_CODE: 'ss',
			COUNTRY_DIAL_CODE: '+211',
			COUNTRY_FLAG: 'ss.svg',
		},
		{
			COUNTRY_NAME: 'Spain (España)',
			COUNTRY_CODE: 'es',
			COUNTRY_DIAL_CODE: '+34',
			COUNTRY_FLAG: 'es.svg',
		},
		{
			COUNTRY_NAME: 'Sri Lanka (ශ්‍රී ලංකාව)',
			COUNTRY_CODE: 'lk',
			COUNTRY_DIAL_CODE: '+94',
			COUNTRY_FLAG: 'lk.svg',
		},
		{
			COUNTRY_NAME: 'Sudan (‫السودان‬‎)',
			COUNTRY_CODE: 'sd',
			COUNTRY_DIAL_CODE: '+249',
			COUNTRY_FLAG: 'sd.svg',
		},
		{
			COUNTRY_NAME: 'Suriname',
			COUNTRY_CODE: 'sr',
			COUNTRY_DIAL_CODE: '+597',
			COUNTRY_FLAG: 'sr.svg',
		},
		{
			COUNTRY_NAME: 'Svalbard and Jan Mayen',
			COUNTRY_CODE: 'sj',
			COUNTRY_DIAL_CODE: '+47',
			COUNTRY_FLAG: 'sj.svg',
		},
		{
			COUNTRY_NAME: 'Swaziland',
			COUNTRY_CODE: 'sz',
			COUNTRY_DIAL_CODE: '+268',
			COUNTRY_FLAG: 'sz.svg',
		},
		{
			COUNTRY_NAME: 'Sweden (Sverige)',
			COUNTRY_CODE: 'se',
			COUNTRY_DIAL_CODE: '+46',
			COUNTRY_FLAG: 'se.svg',
		},
		{
			COUNTRY_NAME: 'Switzerland (Schweiz)',
			COUNTRY_CODE: 'ch',
			COUNTRY_DIAL_CODE: '+41',
			COUNTRY_FLAG: 'ch.svg',
		},
		{
			COUNTRY_NAME: 'Syria (‫سوريا‬‎)',
			COUNTRY_CODE: 'sy',
			COUNTRY_DIAL_CODE: '+963',
			COUNTRY_FLAG: 'sy.svg',
		},
		{
			COUNTRY_NAME: 'Taiwan (台灣)',
			COUNTRY_CODE: 'tw',
			COUNTRY_DIAL_CODE: '+886',
			COUNTRY_FLAG: 'tw.svg',
		},
		{
			COUNTRY_NAME: 'Tajikistan',
			COUNTRY_CODE: 'tj',
			COUNTRY_DIAL_CODE: '+992',
			COUNTRY_FLAG: 'tj.svg',
		},
		{
			COUNTRY_NAME: 'Tanzania',
			COUNTRY_CODE: 'tz',
			COUNTRY_DIAL_CODE: '+255',
			COUNTRY_FLAG: 'tz.svg',
		},
		{
			COUNTRY_NAME: 'Thailand (ไทย)',
			COUNTRY_CODE: 'th',
			COUNTRY_DIAL_CODE: '+66',
			COUNTRY_FLAG: 'th.svg',
		},
		{
			COUNTRY_NAME: 'Timor-Leste',
			COUNTRY_CODE: 'tl',
			COUNTRY_DIAL_CODE: '+670',
			COUNTRY_FLAG: 'tl.svg',
		},
		{
			COUNTRY_NAME: 'Togo',
			COUNTRY_CODE: 'tg',
			COUNTRY_DIAL_CODE: '+228',
			COUNTRY_FLAG: 'tg.svg',
		},
		{
			COUNTRY_NAME: 'Tokelau',
			COUNTRY_CODE: 'tk',
			COUNTRY_DIAL_CODE: '+690',
			COUNTRY_FLAG: 'tk.svg',
		},
		{
			COUNTRY_NAME: 'Tonga',
			COUNTRY_CODE: 'to',
			COUNTRY_DIAL_CODE: '+676',
			COUNTRY_FLAG: 'to.svg',
		},
		{
			COUNTRY_NAME: 'Trinidad and Tobago',
			COUNTRY_CODE: 'tt',
			COUNTRY_DIAL_CODE: '+1868',
			COUNTRY_FLAG: 'tt.svg',
		},
		{
			COUNTRY_NAME: 'Tunisia (‫تونس‬‎)',
			COUNTRY_CODE: 'tn',
			COUNTRY_DIAL_CODE: '+216',
			COUNTRY_FLAG: 'tn.svg',
		},
		{
			COUNTRY_NAME: 'Turkey (Türkiye)',
			COUNTRY_CODE: 'tr',
			COUNTRY_DIAL_CODE: '+90',
			COUNTRY_FLAG: 'tr.svg',
		},
		{
			COUNTRY_NAME: 'Turkmenistan',
			COUNTRY_CODE: 'tm',
			COUNTRY_DIAL_CODE: '+993',
			COUNTRY_FLAG: 'tm.svg',
		},
		{
			COUNTRY_NAME: 'Turks and Caicos Islands',
			COUNTRY_CODE: 'tc',
			COUNTRY_DIAL_CODE: '+1649',
			COUNTRY_FLAG: 'tc.svg',
		},
		{
			COUNTRY_NAME: 'Tuvalu',
			COUNTRY_CODE: 'tv',
			COUNTRY_DIAL_CODE: '+688',
			COUNTRY_FLAG: 'tv.svg',
		},
		{
			COUNTRY_NAME: 'U.S. Virgin Islands',
			COUNTRY_CODE: 'vi',
			COUNTRY_DIAL_CODE: '+1340',
			COUNTRY_FLAG: 'vi.svg',
		},
		{
			COUNTRY_NAME: 'Uganda',
			COUNTRY_CODE: 'ug',
			COUNTRY_DIAL_CODE: '+256',
			COUNTRY_FLAG: 'ug.svg',
		},
		{
			COUNTRY_NAME: 'Ukraine (Україна)',
			COUNTRY_CODE: 'ua',
			COUNTRY_DIAL_CODE: '+380',
			COUNTRY_FLAG: 'ua.svg',
		},
		{
			COUNTRY_NAME: 'United Arab Emirates (‫الإمارات العربية المتحدة‬‎)',
			COUNTRY_CODE: 'ae',
			COUNTRY_DIAL_CODE: '+971',
			COUNTRY_FLAG: 'ae.svg',
		},
		{
			COUNTRY_NAME: 'United Kingdom',
			COUNTRY_CODE: 'gb',
			COUNTRY_DIAL_CODE: '+44',
			COUNTRY_FLAG: 'gb.svg',
		},
		{
			COUNTRY_NAME: 'United States',
			COUNTRY_CODE: 'us',
			COUNTRY_DIAL_CODE: '+1',
			COUNTRY_FLAG: 'us.svg',
		},
		{
			COUNTRY_NAME: 'Uruguay',
			COUNTRY_CODE: 'uy',
			COUNTRY_DIAL_CODE: '+598',
			COUNTRY_FLAG: 'uy.svg',
		},
		{
			COUNTRY_NAME: 'Uzbekistan (Oʻzbekiston)',
			COUNTRY_CODE: 'uz',
			COUNTRY_DIAL_CODE: '+998',
			COUNTRY_FLAG: 'uz.svg',
		},
		{
			COUNTRY_NAME: 'Vanuatu',
			COUNTRY_CODE: 'vu',
			COUNTRY_DIAL_CODE: '+678',
			COUNTRY_FLAG: 'vu.svg',
		},
		{
			COUNTRY_NAME: 'Vatican City (Città del Vaticano)',
			COUNTRY_CODE: 'va',
			COUNTRY_DIAL_CODE: '+39',
			COUNTRY_FLAG: 'va.svg',
		},
		{
			COUNTRY_NAME: 'Venezuela',
			COUNTRY_CODE: 've',
			COUNTRY_DIAL_CODE: '+58',
			COUNTRY_FLAG: 've.svg',
		},
		{
			COUNTRY_NAME: 'Vietnam (Việt Nam)',
			COUNTRY_CODE: 'vn',
			COUNTRY_DIAL_CODE: '+84',
			COUNTRY_FLAG: 'vn.svg',
		},
		{
			COUNTRY_NAME: 'Wallis and Futuna (Wallis-et-Futuna)',
			COUNTRY_CODE: 'wf',
			COUNTRY_DIAL_CODE: '+681',
			COUNTRY_FLAG: 'wf.svg',
		},
		{
			COUNTRY_NAME: 'Western Sahara (‫الصحراء الغربية‬‎)',
			COUNTRY_CODE: 'eh',
			COUNTRY_DIAL_CODE: '+212',
			COUNTRY_FLAG: 'eh.svg',
		},
		{
			COUNTRY_NAME: 'Yemen (‫اليمن‬‎)',
			COUNTRY_CODE: 'ye',
			COUNTRY_DIAL_CODE: '+967',
			COUNTRY_FLAG: 'ye.svg',
		},
		{
			COUNTRY_NAME: 'Zambia',
			COUNTRY_CODE: 'zm',
			COUNTRY_DIAL_CODE: '+260',
			COUNTRY_FLAG: 'zm.svg',
		},
		{
			COUNTRY_NAME: 'Zimbabwe',
			COUNTRY_CODE: 'zw',
			COUNTRY_DIAL_CODE: '+263',
			COUNTRY_FLAG: 'zw.svg',
		},
		{
			COUNTRY_NAME: 'Åland Islands',
			COUNTRY_CODE: 'ax',
			COUNTRY_DIAL_CODE: '+358',
			COUNTRY_FLAG: 'ax.svg',
		},
	];

	public setSignupStatus(status: boolean) {
		this.adminSignup.next({ status: status });
	}

	public getSignupStatus(): Observable<any> {
		return this.adminSignup.asObservable();
	}

	public setListLayoutData(listLayoutData) {
		this.listLayoutData = listLayoutData;
	}

	public getListLayoutData() {
		return this.listLayoutData;
	}

	public chronometerFormatTransform(
		value: number,
		formattedTime: string
	): string {
		// Conversion rates 1d = 8h, 1w = 5d, 1mo = 20d(4w)
		if (value >= 9600) {
			// 1 Month = 9600 minutes
			const remainder = value % 9600;
			if (remainder === 0) {
				return value / 9600 + 'mo';
			} else {
				formattedTime = Math.floor(value / 9600) + 'mo';
				return this.chronometerFormatTransform(remainder, formattedTime);
			}
		} else if (value >= 2400) {
			// 1 Week = 2400 minutes
			const remainder = value % 2400;
			if (remainder === 0) {
				if (formattedTime.length > 0) {
					return formattedTime + ' ' + value / 2400 + 'w';
				} else {
					return value / 2400 + 'w';
				}
			} else {
				if (formattedTime.length > 0) {
					formattedTime = formattedTime + ' ' + Math.floor(value / 2400) + 'w';
				} else {
					formattedTime = Math.floor(value / 2400) + 'w';
				}
				return this.chronometerFormatTransform(remainder, formattedTime);
			}
		} else if (value >= 480) {
			// 1 Day = 480 minutes
			const remainder = value % 480;
			if (remainder === 0) {
				if (formattedTime.length > 0) {
					return formattedTime + ' ' + value / 480 + 'd';
				} else {
					return value / 480 + 'd';
				}
			} else {
				if (formattedTime.length > 0) {
					formattedTime = formattedTime + ' ' + Math.floor(value / 480) + 'd';
				} else {
					formattedTime = Math.floor(value / 480) + 'd';
				}
				return this.chronometerFormatTransform(remainder, formattedTime);
			}
		} else if (value >= 60) {
			// 1 Hour = 60 minutes
			const remainder = value % 60;
			if (remainder === 0) {
				if (formattedTime.length > 0) {
					return formattedTime + ' ' + value / 60 + 'h';
				} else {
					return value / 60 + 'h';
				}
			} else {
				if (formattedTime.length > 0) {
					formattedTime = formattedTime + ' ' + Math.floor(value / 60) + 'h';
				} else {
					formattedTime = Math.floor(value / 60) + 'h';
				}
				return this.chronometerFormatTransform(remainder, formattedTime);
			}
		} else {
			if (formattedTime.length > 0) {
				return formattedTime + ' ' + value + 'm';
			} else {
				return value + 'm';
			}
		}
	}
}
