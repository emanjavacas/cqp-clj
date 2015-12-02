/**
 * 
 * This software is copyright (c) 2015 by Enrique Manjavacas
 * This is free software. You can redistribute it and/or modify it under the
 * terms described in the GNU General Public License v3 of which you should
 * have received a copy. Otherwise you can download it from
 *
 * http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright 2015 Enrique Manjavacas
 * @copyright 2012 Seminar fuer Sprachwissenschaft (http://www.sfs.uni-tuebingen.de/)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt GNU General Public License
 * v3
 */

public class CqiClientException extends Exception {

    public CqiClientException(String message) {
        super(message);
    }

    public CqiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
