#!/usr/bin/env python

# Roman Yepishev <roman.yepishev@canonical.com>

from httplib2 import Http
from oauth import oauth
from simplejson import loads
from getpass import getpass
from optparse import OptionParser
from socket import gethostname
from urllib import urlencode
from urlparse import urlparse
from cgi import parse_qsl

class OAuthHttpClient(object):

    def __init__(self):
        self.signature_method = oauth.OAuthSignatureMethod_HMAC_SHA1()
        self.consumer = None
        self.token = None
        self.client = Http()

    def set_consumer(self, consumer_key, consumer_secret):
        self.consumer = oauth.OAuthConsumer(consumer_key,
                                            consumer_secret)

    def set_token(self, token, token_secret):
        self.token = oauth.OAuthToken( token, token_secret)

    def _get_oauth_request_header(self, url, method):
        """Get an oauth request header given the token and the url"""
        query = urlparse(url).query

        oauth_request = oauth.OAuthRequest.from_consumer_and_token(
            http_url=url,
            http_method=method,
            oauth_consumer=self.consumer,
            token=self.token,
            parameters=dict(parse_qsl(query))
        )
        oauth_request.sign_request(oauth.OAuthSignatureMethod_HMAC_SHA1(),
                                   self.consumer, self.token)
        return oauth_request.to_header()

    def request(self, url, method="GET", body=None, headers={}):
        oauth_header = self._get_oauth_request_header(url, method)
        headers.update(oauth_header)
        return self.client.request(url, method, headers=headers, body=body)

class Application(object):
    PING_URL = 'https://%s/oauth/sso-finished-so-get-tokens/'
    AUTH_URL = 'https://%s/api/1.0/authentications'

    def __init__(self):
        self.sso_client = Http()
        self.oauth_client = OAuthHttpClient()

    def main(self, options):

        hostname = options.hostname
        ping_url = Application.PING_URL % (options.u1_host, )
        auth_url = Application.AUTH_URL % (options.sso_host, )

        print "Creating new entry for %s" % (hostname)

        username = raw_input('SSO login: ')
        password = getpass('password: ')

        self.sso_client.add_credentials(username, password)

        body = {'ws.op': 'authenticate',
                'token_name': '"Ubuntu One @ %s"' % (hostname, )}

        qs = urlencode(body)

        url = auth_url + '?' + qs
        print "Using SSO URL: " + url

        resp, content = self.sso_client.request(url)
        if resp['status'] != '200':
            print "Login failed"
            print "Server headers:\n" + str(resp)
            print "Server response content:\n" + content
            return

        auth_info = loads(content)
        self.oauth_client.set_consumer(auth_info['consumer_key'],
                                       auth_info['consumer_secret'])

        self.oauth_client.set_token(auth_info['token'],
                                    auth_info['token_secret'])

        print "OAuth info:\n%s:%s:%s:%s" % (
                auth_info['consumer_key'], auth_info['consumer_secret'],
                auth_info['token'], auth_info['token_secret'])

        resp, content = self.oauth_client.request(ping_url + username)
        if resp['status'] != '200':
            print "Error pinging Ubuntu One"
            print "Server headers:\n" + str(resp)
            print "Server response content:\n" + content
            return

        print 'Ping result: ' + content

if __name__ == "__main__":
    parser = OptionParser(usage='%prog [--hostname other]')
    parser.add_option('--hostname', help='Hostname to use',
                      dest="hostname", default=gethostname())
    parser.add_option('--sso-host', help='Ubuntu SSO server to use',
                      dest='sso_host', default='login.ubuntu.com')
    parser.add_option('--u1-host', help='Ubuntu One server to use',
                      dest='u1_host', default='one.ubuntu.com')

    options, args = parser.parse_args()

    app = Application()
    app.main(options)
