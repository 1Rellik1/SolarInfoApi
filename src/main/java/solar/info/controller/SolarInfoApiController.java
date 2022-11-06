package solar.info.controller;

import ca.rmen.sunrisesunset.SunriseSunset;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import solar.info.dto.SolarInfoDTO;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Controller
public class SolarInfoApiController {

    @RequestMapping(value = "/api/sunrise-sunset", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getSolarInfo(@RequestParam double lat, @RequestParam double lng) {
        Calendar[] sunriseSunset = SunriseSunset.getSunriseSunset(Calendar.getInstance(), lat, lng);
        Calendar solarNoon = SunriseSunset.getSolarNoon(Calendar.getInstance(), lat, lng);
        long dayLength = SunriseSunset.getDayLength(Calendar.getInstance(), lat, lng);
        Calendar[] civilTwilight = SunriseSunset.getCivilTwilight(Calendar.getInstance(), lat, lng);
        Calendar[] nauticalTwilight = SunriseSunset.getNauticalTwilight(Calendar.getInstance(), lat, lng);
        Calendar[] astronomicalTwilight = SunriseSunset.getAstronomicalTwilight(Calendar.getInstance(), lat, lng);
        System.out.println(civilTwilight[1].getTime());
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(sunriseSunset[0].getTime());
        SolarInfoDTO solarInfoDTO = new SolarInfoDTO();
        solarInfoDTO.setSunrise(simpleDateFormat.format(sunriseSunset[0].getTime()));
        solarInfoDTO.setSunset(simpleDateFormat.format(sunriseSunset[1].getTime()));
        solarInfoDTO.setSolar_noon(simpleDateFormat.format(solarNoon.getTime()));
        solarInfoDTO.setDay_length(dayLength);
        solarInfoDTO.setCivil_twilight_begin(simpleDateFormat.format(civilTwilight[0].getTime()));
        solarInfoDTO.setCivil_twilight_end(simpleDateFormat.format(civilTwilight[1].getTime()));
        solarInfoDTO.setNautical_twilight_begin(simpleDateFormat.format(nauticalTwilight[0].getTime()));
        solarInfoDTO.setNautical_twilight_end(simpleDateFormat.format(nauticalTwilight[1].getTime()));
        solarInfoDTO.setAstronomical_twilight_begin(simpleDateFormat.format(astronomicalTwilight[0].getTime()));
        solarInfoDTO.setAstronomical_twilight_end(simpleDateFormat.format(astronomicalTwilight[1].getTime()));
        return new ResponseEntity(solarInfoDTO, HttpStatus.OK);
    }

}
